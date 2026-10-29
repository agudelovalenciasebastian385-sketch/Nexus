package nexusmarket.domain.services.warehouse;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.Test;

class WarehouseServicesTest {

    private static final Address NEW_LOCATION = new Address("Avenida 68 # 1-1", null, "Bogota", "Cundinamarca",
            "Colombia");

    private final ServiceHarness h = new ServiceHarness();
    private final Administrator admin = h.admin();
    private final Seller seller = h.seller();
    private final Warehouse sellerWarehouse = h.firstWarehouse(seller);

    @Test
    void administratorRegistersAMarketplaceWarehouse() {
        Warehouse warehouse = h.registerWarehouse.registerWarehouse(admin, "Hub", ADDRESS, WarehouseType.MARKETPLACE);

        assertThat(warehouse.isMarketplace()).isTrue();
        assertThat(h.warehouses.findById(warehouse.getId())).contains(warehouse);
        assertThat(h.audit.actions()).contains(AuditAction.WAREHOUSE_REGISTERED);
    }

    @Test
    void administratorCannotRegisterASellerWarehouse() {
        assertThatThrownBy(() -> h.registerWarehouse.registerWarehouse(admin, "Store", ADDRESS, WarehouseType.SELLER))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerRegistersItsOwnWarehouse() {
        Warehouse warehouse = h.registerWarehouse.registerWarehouse(seller, "Second", ADDRESS, WarehouseType.SELLER);

        assertThat(warehouse.getSeller()).isEqualTo(seller);
        assertThat(seller.getWarehouses()).contains(warehouse);
    }

    @Test
    void sellerCannotRegisterAMarketplaceWarehouse() {
        assertThatThrownBy(() -> h.registerWarehouse.registerWarehouse(seller, "Hub", ADDRESS,
                WarehouseType.MARKETPLACE)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void warehouseTypeIsRequired() {
        assertThatThrownBy(() -> h.registerWarehouse.registerWarehouse(admin, "Hub", ADDRESS, null))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void buyerCannotRegisterWarehouses() {
        assertThatThrownBy(() -> h.registerWarehouse.registerWarehouse(h.buyer(), "Hub", ADDRESS,
                WarehouseType.MARKETPLACE)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerConsultsItsWarehouse() {
        assertThat(h.consultWarehouse.consultWarehouse(seller, sellerWarehouse.getId())).isSameAs(sellerWarehouse);
    }

    @Test
    void sellerCannotConsultAnotherSellersWarehouse() {
        assertThatThrownBy(() -> h.consultWarehouse.consultWarehouse(h.seller(), sellerWarehouse.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void consultingAnUnknownWarehouseIsReported() {
        assertThatThrownBy(() -> h.consultWarehouse.consultWarehouse(admin, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void sellerListsOnlyItsWarehouses() {
        h.marketplaceWarehouse();
        h.seller();

        assertThat(h.consultWarehouse.consultWarehouses(seller)).containsExactly(sellerWarehouse);
    }

    @Test
    void operatorListsAllWarehouses() {
        h.marketplaceWarehouse();

        assertThat(h.consultWarehouse.consultWarehouses(h.operator())).hasSize(2);
    }

    @Test
    void sellerUpdatesItsWarehouseAndItIsAudited() {
        h.updateWarehouse.updateWarehouse(seller, sellerWarehouse.getId(), "Renamed", NEW_LOCATION);

        assertThat(sellerWarehouse.getName()).isEqualTo("Renamed");
        assertThat(sellerWarehouse.getLocation()).isEqualTo(NEW_LOCATION);
        assertThat(h.audit.actions()).contains(AuditAction.WAREHOUSE_UPDATED);
    }

    @Test
    void administratorUpdatesAMarketplaceWarehouse() {
        Warehouse hub = h.marketplaceWarehouse();

        h.updateWarehouse.updateWarehouse(admin, hub.getId(), "Main Hub", NEW_LOCATION);

        assertThat(hub.getName()).isEqualTo("Main Hub");
    }

    @Test
    void administratorCannotUpdateASellerWarehouse() {
        assertThatThrownBy(() -> h.updateWarehouse.updateWarehouse(admin, sellerWarehouse.getId(), "X", NEW_LOCATION))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerCannotUpdateAnotherSellersWarehouse() {
        assertThatThrownBy(() -> h.updateWarehouse.updateWarehouse(h.seller(), sellerWarehouse.getId(), "X",
                NEW_LOCATION)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void updatingAnUnknownWarehouseIsReported() {
        assertThatThrownBy(() -> h.updateWarehouse.updateWarehouse(admin, "missing", "X", NEW_LOCATION))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
