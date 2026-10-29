package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.marketplaceWarehouse;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.Test;

class WarehouseTest {

    @Test
    void sellerWarehouseWithoutSellerIsRejected() {
        assertThatThrownBy(() -> new Warehouse("w-1", "Store", ADDRESS, WarehouseType.SELLER, null))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void marketplaceWarehouseWithSellerIsRejected() {
        assertThatThrownBy(() -> new Warehouse("w-1", "Hub", ADDRESS, WarehouseType.MARKETPLACE, seller()))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> new Warehouse("w-1", " ", ADDRESS, WarehouseType.MARKETPLACE, null))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void locationIsRequired() {
        assertThatThrownBy(() -> new Warehouse("w-1", "Hub", null, WarehouseType.MARKETPLACE, null))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void marketplaceWarehouseIsNotSellerOwned() {
        Warehouse warehouse = marketplaceWarehouse();

        assertThat(warehouse.isMarketplace()).isTrue();
        assertThat(warehouse.isSellerOwned()).isFalse();
    }

    @Test
    void sellerWarehouseIsSellerOwned() {
        assertThat(sellerWarehouse(seller()).isSellerOwned()).isTrue();
    }
}
