package nexusmarket.adapters.persistence.jpa.mappers;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.RefundStatus;
import nexusmarket.domain.valueobjects.ReturnStatus;
import nexusmarket.domain.valueobjects.ShipmentStatus;
import org.junit.jupiter.api.Test;

class ShipmentAndRefundJpaMapperTest {

    private static final BigDecimal AMOUNT = new BigDecimal("300.00");

    private final Order order = DomainFixtures.physicalOrder(OrderStatus.DELIVERED);
    private final Warehouse warehouse = DomainFixtures.marketplaceWarehouse();
    private final LogisticsOperator operator = DomainFixtures.operator();
    private final Shipment shipment = new Shipment("shipment-1", order, warehouse, ADDRESS, operator,
            ShipmentStatus.IN_TRANSIT, NOW);
    private final ReturnRequest returnRequest = new ReturnRequest("return-1", order, "Wrong color",
            ReturnStatus.APPROVED);

    private Shipment restoredShipment() {
        return ShipmentJpaMapper.toDomain(ShipmentJpaMapper.toEntity(shipment), order, warehouse, operator);
    }

    private Refund restoredRefund(Refund refund) {
        return RefundJpaMapper.toDomain(RefundJpaMapper.toEntity(refund), returnRequest);
    }

    @Test
    void shipmentKeepsItsCreationDate() {
        assertThat(restoredShipment().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void shipmentKeepsItsStatus() {
        assertThat(restoredShipment().getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void shipmentKeepsItsDeliveryAddress() {
        assertThat(restoredShipment().getDeliveryAddress()).isEqualTo(ADDRESS);
    }

    @Test
    void shipmentStoresItsReferencesAsIdentifiers() {
        assertThat(ShipmentJpaMapper.toEntity(shipment).getLogisticsOperatorId()).isEqualTo(operator.getId());
    }

    @Test
    void processedRefundKeepsItsProcessedDate() {
        Refund processed = new Refund("refund-1", returnRequest, AMOUNT, RefundStatus.PROCESSED, NOW);

        assertThat(restoredRefund(processed).getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    void pendingRefundStaysWithoutProcessedDate() {
        assertThat(restoredRefund(new Refund("refund-1", returnRequest, AMOUNT)).getProcessedAt()).isNull();
    }

    @Test
    void refundKeepsItsAmountAndStatus() {
        Refund restored = restoredRefund(new Refund("refund-1", returnRequest, AMOUNT));

        assertThat(restored.getAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(restored.getStatus()).isEqualTo(RefundStatus.PENDING);
    }

    @Test
    void refundStoresItsReturnRequestAsAnIdentifier() {
        assertThat(RefundJpaMapper.toEntity(new Refund("refund-1", returnRequest, AMOUNT)).getReturnRequestId())
                .isEqualTo("return-1");
    }
}
