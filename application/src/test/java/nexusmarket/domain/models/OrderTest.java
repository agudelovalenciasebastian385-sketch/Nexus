package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.buyer;
import static nexusmarket.domain.DomainFixtures.digitalItem;
import static nexusmarket.domain.DomainFixtures.digitalOrder;
import static nexusmarket.domain.DomainFixtures.operator;
import static nexusmarket.domain.DomainFixtures.physicalItem;
import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static nexusmarket.domain.DomainFixtures.publishedDigital;
import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.exceptions.InvalidInvoiceException;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.exceptions.InvalidOrderStatusTransitionException;
import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.exceptions.OrderAlreadyFinalizedException;
import nexusmarket.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static Shipment shipmentOf(Order order) {
        Seller seller = seller();
        return new Shipment("shipment-" + order.getId(), order, sellerWarehouse(seller), ADDRESS, operator(), NOW);
    }

    // ------------------------------------------------------------------ valid transitions

    @Test
    void newOrderStartsPendingPayment() {
        assertThat(physicalOrder(buyer()).getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void paymentMovesPendingOrderToPaid() {
        Order order = physicalOrder(OrderStatus.PENDING_PAYMENT);

        order.markPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shippingMovesPaidOrderToShipped() {
        Order order = physicalOrder(OrderStatus.PAID);

        order.markShipped();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void deliveringMovesShippedOrderToDelivered() {
        Order order = physicalOrder(OrderStatus.SHIPPED);

        order.markDelivered();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void paidDigitalOnlyOrderCanBeDeliveredDirectly() {
        Order order = digitalOrder(buyer());
        order.markPaid();

        order.markDelivered();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    // ------------------------------------------------------------------ invalid transitions

    @Test
    void paidOrderWithPhysicalItemsCannotSkipShipping() {
        Order order = physicalOrder(OrderStatus.PAID);

        assertThatThrownBy(order::markDelivered).isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void payingAnOrderThatIsNotPendingPaymentIsRejected() {
        Order order = physicalOrder(OrderStatus.PAID);

        assertThatThrownBy(order::markPaid).isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void shippingAnUnpaidOrderIsRejected() {
        Order order = physicalOrder(OrderStatus.PENDING_PAYMENT);

        assertThatThrownBy(order::markShipped).isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void deliveringAnUnpaidOrderIsRejected() {
        Order order = physicalOrder(OrderStatus.PENDING_PAYMENT);

        assertThatThrownBy(order::markDelivered).isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    // ------------------------------------------------------------------ delivered order is final

    @Test
    void deliveredOrderCannotChangeStatus() {
        Order order = physicalOrder(OrderStatus.DELIVERED);

        assertThatThrownBy(order::markPaid).isInstanceOf(OrderAlreadyFinalizedException.class);
    }

    @Test
    void deliveredOrderRejectsAnInvoice() {
        Order order = physicalOrder(OrderStatus.DELIVERED);
        Invoice invoice = new Invoice("invoice-1", order, NOW);

        assertThatThrownBy(() -> order.attachInvoice(invoice)).isInstanceOf(OrderAlreadyFinalizedException.class);
    }

    @Test
    void deliveredOrderRejectsAShipment() {
        Order order = physicalOrder(OrderStatus.DELIVERED);

        assertThatThrownBy(() -> order.addShipment(shipmentOf(order)))
                .isInstanceOf(OrderAlreadyFinalizedException.class);
    }

    @Test
    void deliveredOrderIsFinalized() {
        assertThat(physicalOrder(OrderStatus.DELIVERED).isFinalized()).isTrue();
    }

    // ------------------------------------------------------------------ invoice and shipments

    @Test
    void orderAcceptsItsInvoice() {
        Order order = physicalOrder(OrderStatus.PAID);
        Invoice invoice = new Invoice("invoice-1", order, NOW);

        order.attachInvoice(invoice);

        assertThat(order.getInvoice()).isSameAs(invoice);
    }

    @Test
    void secondInvoiceIsRejected() {
        Order order = physicalOrder(OrderStatus.PAID);
        order.attachInvoice(new Invoice("invoice-1", order, NOW));

        assertThatThrownBy(() -> order.attachInvoice(new Invoice("invoice-2", order, NOW)))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void invoiceOfAnotherOrderIsRejected() {
        Order order = physicalOrder(OrderStatus.PAID);
        Invoice foreign = new Invoice("invoice-1", physicalOrder(OrderStatus.PAID), NOW);

        assertThatThrownBy(() -> order.attachInvoice(foreign)).isInstanceOf(InvalidInvoiceException.class);
    }

    @Test
    void shipmentOfAnotherOrderIsRejected() {
        Order order = physicalOrder(OrderStatus.PAID);
        Shipment foreign = shipmentOf(physicalOrder(OrderStatus.PAID));

        assertThatThrownBy(() -> order.addShipment(foreign)).isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void rehydratedOrderKeepsShipmentsBuiltForTheSameOrderId() {
        Order base = physicalOrder(OrderStatus.PAID);
        Shipment shipment = shipmentOf(base);

        Order rehydrated = new Order(base.getId(), base.getBuyer(), base.getItems(), OrderStatus.SHIPPED, NOW, null,
                List.of(shipment));

        assertThat(rehydrated.getShipments()).containsExactly(shipment);
    }

    // ------------------------------------------------------------------ items and total

    @Test
    void totalAmountIsTheSumOfItemSubtotals() {
        Seller seller = seller();
        PhysicalProduct mouse = publishedPhysical(seller);
        DigitalProduct ebook = publishedDigital(seller);
        Order order = new Order("order-1", buyer(),
                List.of(physicalItem(mouse, sellerWarehouse(seller), 2), digitalItem(ebook, 3)), NOW);

        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    void orderWithoutItemsIsRejected() {
        assertThatThrownBy(() -> new Order("order-1", buyer(), List.of(), NOW))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void orderWithAPhysicalItemHasPhysicalItems() {
        assertThat(physicalOrder(buyer()).hasPhysicalItems()).isTrue();
    }

    @Test
    void digitalOnlyOrderHasNoPhysicalItems() {
        assertThat(digitalOrder(buyer()).hasPhysicalItems()).isFalse();
    }
}
