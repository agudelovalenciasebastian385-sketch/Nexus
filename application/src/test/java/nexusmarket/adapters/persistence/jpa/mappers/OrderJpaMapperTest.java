package nexusmarket.adapters.persistence.jpa.mappers;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;

class OrderJpaMapperTest {

    private static final Variant BLACK = new Variant("color", "black");
    private static final BigDecimal PRICE = new BigDecimal("150.00");

    private final Seller seller = DomainFixtures.seller();
    private final Buyer buyer = DomainFixtures.buyer();
    private final Warehouse warehouse = DomainFixtures.sellerWarehouse(seller);
    private final PhysicalProduct mouse = DomainFixtures.publishedPhysical(seller, BLACK);
    private final DigitalProduct ebook = DomainFixtures.publishedDigital(seller);
    private final OrderItem physicalItem = new OrderItem(mouse, BLACK, 2, PRICE, warehouse);
    private final OrderItem digitalItem = new OrderItem(ebook, null, 1, PRICE, null);
    private final List<OrderItem> items = List.of(physicalItem, digitalItem);
    private final Order order = new Order("order-1", buyer, items, OrderStatus.SHIPPED, NOW, null, null);

    private Order restored(List<Shipment> shipments) {
        return OrderJpaMapper.toDomain(OrderJpaMapper.toEntity(order), buyer, items, shipments);
    }

    @Test
    void statusSurvivesTheRoundTrip() {
        assertThat(restored(null).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void creationDateSurvivesTheRoundTrip() {
        assertThat(restored(null).getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void buyerIsStoredAsAnIdentifier() {
        assertThat(OrderJpaMapper.toEntity(order).getBuyerId()).isEqualTo(buyer.getId());
    }

    @Test
    void itemsAreRestored() {
        assertThat(restored(null).getItems()).containsExactlyElementsOf(items);
    }

    @Test
    void shipmentsAreRestored() {
        Shipment shipment = new Shipment("shipment-1", order, warehouse, ADDRESS, DomainFixtures.operator(), NOW);

        assertThat(restored(List.of(shipment)).getShipments()).containsExactly(shipment);
    }

    @Test
    void loadedOrderNeverCarriesItsInvoice() {
        assertThat(restored(null).getInvoice()).isNull();
    }

    @Test
    void copyToEntityUpdatesTheStatus() {
        OrderJpaEntity managed = new OrderJpaEntity();

        OrderJpaMapper.copyToEntity(order, managed);

        assertThat(managed.getStatus()).isEqualTo("SHIPPED");
    }

    @Test
    void copyToEntityKeepsTheVersionReadAtTheStartOfTheUseCase() {
        OrderJpaEntity managed = new OrderJpaEntity();
        managed.setVersion(3L);

        OrderJpaMapper.copyToEntity(order, managed);

        assertThat(managed.getVersion()).isEqualTo(3L);
    }

    @Test
    void physicalItemSurvivesTheRoundTrip() {
        OrderItem item = OrderItemJpaMapper.toDomain(OrderItemJpaMapper.toEntity(physicalItem, "order-1"), mouse,
                warehouse);

        assertThat(item).usingRecursiveComparison().isEqualTo(physicalItem);
    }

    @Test
    void itemReferencesItsOrder() {
        assertThat(OrderItemJpaMapper.toEntity(physicalItem, "order-1").getOrderId()).isEqualTo("order-1");
    }

    @Test
    void physicalItemStoresItsOriginWarehouse() {
        assertThat(OrderItemJpaMapper.toEntity(physicalItem, "order-1").getOriginWarehouseId())
                .isEqualTo(warehouse.getId());
    }

    @Test
    void digitalItemHasNoOriginWarehouse() {
        OrderItemJpaEntity entity = OrderItemJpaMapper.toEntity(digitalItem, "order-1");

        assertThat(OrderItemJpaMapper.toDomain(entity, ebook, null).getOriginWarehouse()).isNull();
    }

    @Test
    void itemWithoutVariantStaysWithoutVariant() {
        OrderItemJpaEntity entity = OrderItemJpaMapper.toEntity(digitalItem, "order-1");

        assertThat(OrderItemJpaMapper.toDomain(entity, ebook, null).getVariant()).isNull();
    }
}
