package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import org.junit.jupiter.api.Test;

class SellerTest {

    private final Seller seller = seller();

    @Test
    void ownWarehouseIsAdded() {
        Warehouse warehouse = sellerWarehouse(seller);

        seller.addWarehouse(warehouse);

        assertThat(seller.getWarehouses()).containsExactly(warehouse);
    }

    @Test
    void warehouseOfAnotherSellerIsRejected() {
        assertThatThrownBy(() -> seller.addWarehouse(sellerWarehouse(seller())))
                .isInstanceOf(InvalidWarehouseException.class);
    }

    @Test
    void ownProductIsAdded() {
        PhysicalProduct product = publishedPhysical(seller);

        seller.addProduct(product);

        assertThat(seller.getProducts()).containsExactly(product);
    }

    @Test
    void productOfAnotherSellerIsRejected() {
        assertThatThrownBy(() -> seller.addProduct(publishedPhysical(seller())))
                .isInstanceOf(InvalidProductException.class);
    }
}
