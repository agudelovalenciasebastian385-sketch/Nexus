package nexusmarket.domain.services.shipment;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.digitalItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ShipmentStatus;
import org.junit.jupiter.api.Test;

class ShipmentServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Buyer buyer = h.buyer();
    private final LogisticsOperator operator = h.operator();

    ShipmentServicesTest() {
        h.stock(mouse, warehouse, 10);
    }

    private Shipment createdShipment(Order order) {
        return h.createShipments.createShipments(operator, order.getId()).get(0);
    }

    private Shipment dispatchedShipment(Order order) {
        Shipment shipment = createdShipment(order);
        h.packShipment.packShipment(operator, shipment.getId());
        h.dispatchShipment.dispatchShipment(operator, shipment.getId());
        return shipment;
    }

    // ------------------------------------------------------------------ create

    @Test
    void oneShipmentIsCreatedPerOriginWarehouse() {
        Warehouse second = h.warehouseOf(seller);
        PhysicalProduct lamp = h.physicalProduct(seller, "50.00");
        h.stock(lamp, second, 5);
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 1);
        Order order = h.paidOrder(buyer, lamp, 1);

        List<Shipment> shipments = h.createShipments.createShipments(operator, order.getId());

        assertThat(shipments).extracting(Shipment::getOriginWarehouse).containsExactlyInAnyOrder(warehouse, second);
    }

    @Test
    void newShipmentIsPreparingToTheBuyerAddressAndAssignedToTheOperator() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PREPARING);
        assertThat(shipment.getDeliveryAddress()).isEqualTo(buyer.getPrimaryAddress());
        assertThat(shipment.getLogisticsOperator()).isEqualTo(operator);
    }

    @Test
    void creatingShipmentsIsAudited() {
        createdShipment(h.paidOrder(buyer, mouse, 1));

        assertThat(h.audit.actions()).contains(AuditAction.SHIPMENT_CREATED);
    }

    @Test
    void shipmentsCannotBeCreatedForAnUnpaidOrder() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.createShipments.createShipments(operator, order.getId()))
                .isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void shipmentsCannotBeCreatedForAPaidOrderWithoutPhysicalItems() {
        Order digital = h.orders.save(new Order("order-d", buyer,
                List.of(digitalItem(h.digitalProduct(seller, "10.00"), 1)), OrderStatus.PAID, NOW, null, null));

        assertThatThrownBy(() -> h.createShipments.createShipments(operator, digital.getId()))
                .isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void shipmentsCannotBeCreatedTwice() {
        Order order = h.paidOrder(buyer, mouse, 1);
        createdShipment(order);

        assertThatThrownBy(() -> h.createShipments.createShipments(operator, order.getId()))
                .isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void sellerCannotCreateShipments() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.createShipments.createShipments(seller, order.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void shipmentsForAnUnknownOrderAreReported() {
        assertThatThrownBy(() -> h.createShipments.createShipments(operator, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ------------------------------------------------------------------ pack

    @Test
    void packingMarksTheShipmentPackedAndIsAudited() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));

        h.packShipment.packShipment(operator, shipment.getId());

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PACKED);
        assertThat(h.audit.actions()).contains(AuditAction.SHIPMENT_PACKED);
    }

    @Test
    void anotherOperatorCannotPackTheShipment() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));

        assertThatThrownBy(() -> h.packShipment.packShipment(h.operator(), shipment.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void packingTwiceIsRejected() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));
        h.packShipment.packShipment(operator, shipment.getId());

        assertThatThrownBy(() -> h.packShipment.packShipment(operator, shipment.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void packingAnUnknownShipmentIsReported() {
        assertThatThrownBy(() -> h.packShipment.packShipment(operator, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ------------------------------------------------------------------ dispatch

    @Test
    void dispatchingPutsTheShipmentInTransitAndTheOrderShipped() {
        Order order = h.paidOrder(buyer, mouse, 2);

        Shipment shipment = dispatchedShipment(order);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void dispatchingConsumesTheReservedStock() {
        dispatchedShipment(h.paidOrder(buyer, mouse, 2));

        assertThat(h.inventoryOf(mouse, warehouse).getQuantity()).isEqualTo(8);
        assertThat(h.inventoryOf(mouse, warehouse).getReservedQuantity()).isZero();
    }

    @Test
    void dispatchingIsAudited() {
        dispatchedShipment(h.paidOrder(buyer, mouse, 1));

        assertThat(h.audit.last(AuditAction.SHIPMENT_DISPATCHED).getDetails()).containsEntry("orderStatus", "SHIPPED");
    }

    @Test
    void dispatchingBeforePackingIsRejected() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));

        assertThatThrownBy(() -> h.dispatchShipment.dispatchShipment(operator, shipment.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void anotherOperatorCannotDispatchTheShipment() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));
        h.packShipment.packShipment(operator, shipment.getId());

        assertThatThrownBy(() -> h.dispatchShipment.dispatchShipment(h.operator(), shipment.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    // ------------------------------------------------------------------ deliver

    @Test
    void deliveringTheLastShipmentDeliversTheOrder() {
        Order order = h.paidOrder(buyer, mouse, 1);
        Shipment shipment = dispatchedShipment(order);

        h.confirmDelivery.confirmDelivery(operator, shipment.getId());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(h.audit.actions()).containsSubsequence(AuditAction.SHIPMENT_DELIVERED, AuditAction.ORDER_DELIVERED);
    }

    @Test
    void deliveringOneOfTwoShipmentsKeepsTheOrderShipped() {
        Warehouse second = h.warehouseOf(seller);
        PhysicalProduct lamp = h.physicalProduct(seller, "50.00");
        h.stock(lamp, second, 5);
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 1);
        Order order = h.paidOrder(buyer, lamp, 1);
        List<Shipment> shipments = h.createShipments.createShipments(operator, order.getId());
        for (Shipment shipment : shipments) {
            h.packShipment.packShipment(operator, shipment.getId());
            h.dispatchShipment.dispatchShipment(operator, shipment.getId());
        }

        h.confirmDelivery.confirmDelivery(operator, shipments.get(0).getId());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void deliveringBeforeDispatchingIsRejected() {
        Shipment shipment = createdShipment(h.paidOrder(buyer, mouse, 1));

        assertThatThrownBy(() -> h.confirmDelivery.confirmDelivery(operator, shipment.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void anotherOperatorCannotConfirmTheDelivery() {
        Shipment shipment = dispatchedShipment(h.paidOrder(buyer, mouse, 1));

        assertThatThrownBy(() -> h.confirmDelivery.confirmDelivery(h.operator(), shipment.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void blockedOperatorCannotConfirmTheDelivery() {
        Shipment shipment = dispatchedShipment(h.paidOrder(buyer, mouse, 1));
        h.blocked(operator);

        assertThatThrownBy(() -> h.confirmDelivery.confirmDelivery(operator, shipment.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void deliveringAnUnknownShipmentIsReported() {
        assertThatThrownBy(() -> h.confirmDelivery.confirmDelivery(operator, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
