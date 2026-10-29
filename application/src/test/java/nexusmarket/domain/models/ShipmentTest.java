package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.operator;
import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ShipmentStatus;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    private final Order order = physicalOrder(OrderStatus.PAID);
    private final Warehouse warehouse = sellerWarehouse(seller());

    private Shipment shipment(ShipmentStatus status) {
        return new Shipment("shipment-1", order, warehouse, ADDRESS, operator(), status, NOW);
    }

    @Test
    void newShipmentStartsPreparing() {
        assertThat(new Shipment("shipment-1", order, warehouse, ADDRESS, operator(), NOW).getStatus())
                .isEqualTo(ShipmentStatus.PREPARING);
    }

    @Test
    void preparingShipmentCanBePacked() {
        Shipment shipment = shipment(ShipmentStatus.PREPARING);

        shipment.markPacked();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PACKED);
    }

    @Test
    void packedShipmentCanBeDispatched() {
        Shipment shipment = shipment(ShipmentStatus.PACKED);

        shipment.markInTransit();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void shipmentInTransitCanBeDelivered() {
        Shipment shipment = shipment(ShipmentStatus.IN_TRANSIT);

        shipment.markDelivered();

        assertThat(shipment.isDelivered()).isTrue();
    }

    @Test
    void packingTwiceIsRejected() {
        Shipment shipment = shipment(ShipmentStatus.PACKED);

        assertThatThrownBy(shipment::markPacked).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void dispatchingBeforePackingIsRejected() {
        Shipment shipment = shipment(ShipmentStatus.PREPARING);

        assertThatThrownBy(shipment::markInTransit).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void deliveringBeforeDispatchingIsRejected() {
        Shipment shipment = shipment(ShipmentStatus.PACKED);

        assertThatThrownBy(shipment::markDelivered).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void logisticsOperatorIsRequired() {
        assertThatThrownBy(() -> new Shipment("shipment-1", order, warehouse, ADDRESS, null, NOW))
                .isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void deliveryAddressIsRequired() {
        assertThatThrownBy(() -> new Shipment("shipment-1", order, warehouse, null, operator(), NOW))
                .isInstanceOf(InvalidShipmentException.class);
    }

    @Test
    void rehydratedShipmentRequiresItsCreationDate() {
        assertThatThrownBy(() -> new Shipment("shipment-1", order, warehouse, ADDRESS, operator(),
                ShipmentStatus.PACKED, null)).isInstanceOf(InvalidShipmentException.class);
    }
}
