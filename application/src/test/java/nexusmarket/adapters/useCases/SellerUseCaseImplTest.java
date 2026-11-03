package nexusmarket.adapters.useCases;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.Test;

class SellerUseCaseImplTest {

    private static final BigDecimal PRICE = new BigDecimal("150.00");
    private static final ReportPeriod PERIOD = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    private final ServiceHarness h = new ServiceHarness();
    private final SellerUseCaseImpl useCase = UseCaseFactory.seller(h);
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");

    @Test
    void consultUserReturnsTheOwnUser() {
        assertThat(useCase.consultUser(seller, seller.getId())).isSameAs(seller);
    }

    @Test
    void changeUserPasswordStoresTheNewHash() {
        Seller registered = h.registerSeller.registerSeller(h.admin(), "1050", "Tech Store", "s@gmail.com",
                "Current123*", "Main", ADDRESS);

        useCase.changeUserPassword(registered, "Current123*", "Newpass123*");

        assertThat(registered.getPassword()).isEqualTo(FakePasswordService.hash("Newpass123*"));
    }

    @Test
    void consultSellerReturnsTheOwnProfile() {
        assertThat(useCase.consultSeller(seller, seller.getId())).isSameAs(seller);
    }

    @Test
    void updateSellerChangesTheName() {
        useCase.updateSeller(seller, seller.getId(), "New Name");

        assertThat(seller.getFullName()).isEqualTo("New Name");
    }

    @Test
    void registerWarehouseStoresTheWarehouse() {
        Warehouse registered = useCase.registerWarehouse(seller, "Hub", ADDRESS, WarehouseType.SELLER);

        assertThat(h.warehouses.findById(registered.getId())).contains(registered);
    }

    @Test
    void consultWarehouseReturnsTheWarehouse() {
        assertThat(useCase.consultWarehouse(seller, warehouse.getId())).isSameAs(warehouse);
    }

    @Test
    void consultWarehousesReturnsTheOwnWarehouses() {
        assertThat(useCase.consultWarehouses(seller)).containsExactly(warehouse);
    }

    @Test
    void updateWarehouseChangesTheName() {
        useCase.updateWarehouse(seller, warehouse.getId(), "New Hub", ADDRESS);

        assertThat(warehouse.getName()).isEqualTo("New Hub");
    }

    @Test
    void registerProductStoresTheProduct() {
        Product product = useCase.registerProduct(seller, ProductType.PHYSICAL, "Lamp", "Desk lamp", PRICE, List.of());

        assertThat(h.products.findById(product.getId())).contains(product);
    }

    @Test
    void updateProductChangesTheName() {
        PhysicalProduct draft = h.productWithStatus(seller, ProductStatus.DRAFT);

        useCase.updateProduct(seller, draft.getId(), "Lamp", "Desk lamp", PRICE, List.of());

        assertThat(draft.getName()).isEqualTo("Lamp");
    }

    @Test
    void publishProductChangesTheStatus() {
        PhysicalProduct draft = h.productWithStatus(seller, ProductStatus.DRAFT);

        assertThat(useCase.publishProduct(seller, draft.getId()).getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void suspendProductChangesTheStatus() {
        assertThat(useCase.suspendProduct(seller, mouse.getId()).getStatus()).isEqualTo(ProductStatus.SUSPENDED);
    }

    @Test
    void discontinueProductChangesTheStatus() {
        assertThat(useCase.discontinueProduct(seller, mouse.getId()).getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    @Test
    void consultSellerProductsReturnsTheOwnProducts() {
        assertThat(useCase.consultSellerProducts(seller, seller.getId())).containsExactly(mouse);
    }

    @Test
    void registerInitialStockStoresTheInventory() {
        Inventory inventory = useCase.registerInitialStock(seller, mouse.getId(), warehouse.getId(), 10);

        assertThat(h.inventoryOf(mouse, warehouse)).isSameAs(inventory);
    }

    @Test
    void registerInventoryInboundAddsTheQuantity() {
        h.stock(mouse, warehouse, 10);

        assertThat(useCase.registerInventoryInbound(seller, mouse.getId(), warehouse.getId(), 5).getQuantity()).isEqualTo(15);
    }

    @Test
    void consultProductInventoryReturnsTheRecords() {
        Inventory inventory = h.stock(mouse, warehouse, 10);

        assertThat(useCase.consultProductInventory(seller, mouse.getId())).containsExactly(inventory);
    }

    @Test
    void consultWarehouseInventoryReturnsTheRecords() {
        Inventory inventory = h.stock(mouse, warehouse, 10);

        assertThat(useCase.consultWarehouseInventory(seller, warehouse.getId())).containsExactly(inventory);
    }

    @Test
    void consultOrderReturnsAnOrderWithOwnProducts() {
        h.stock(mouse, warehouse, 10);
        Buyer buyer = h.buyer();
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThat(useCase.consultOrder(seller, order.getId())).isSameAs(order);
    }

    @Test
    void consultSellerPerformanceReportIsOfTheSeller() {
        SellerPerformanceReport report = useCase.consultSellerPerformanceReport(seller, seller.getId(), PERIOD);

        assertThat(report.getSeller()).isSameAs(seller);
    }
}
