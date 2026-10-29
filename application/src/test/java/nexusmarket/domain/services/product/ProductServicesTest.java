package nexusmarket.domain.services.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProductServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();

    private Product registered(ProductType type) {
        return h.registerProduct.registerProduct(seller, type, "Wireless Mouse", "Ergonomic mouse",
                new BigDecimal("100.00"), List.of(new Variant("color", "black")));
    }

    @Nested
    class Register {

        @Test
        void sellerRegistersAPhysicalProductAsDraft() {
            Product product = registered(ProductType.PHYSICAL);

            assertThat(product).isInstanceOf(PhysicalProduct.class);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
            assertThat(h.products.findById(product.getId())).contains(product);
        }

        @Test
        void sellerRegistersADigitalProduct() {
            assertThat(registered(ProductType.DIGITAL)).isInstanceOf(DigitalProduct.class);
        }

        @Test
        void registrationIsAudited() {
            registered(ProductType.PHYSICAL);

            assertThat(h.audit.last(AuditAction.PRODUCT_REGISTERED).getDetails()).containsEntry("type", "PHYSICAL");
        }

        @Test
        void productTypeIsRequired() {
            assertThatThrownBy(() -> registered(null)).isInstanceOf(InvalidProductException.class);
        }

        @Test
        void buyerCannotRegisterProducts() {
            assertThatThrownBy(() -> h.registerProduct.registerProduct(h.buyer(), ProductType.PHYSICAL, "Mouse",
                    "Mouse", BigDecimal.ONE, List.of())).isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class UpdateAndLifecycle {

        private final Product product = registered(ProductType.PHYSICAL);

        @Test
        void sellerUpdatesItsProductAndIsAudited() {
            h.updateProduct.updateProduct(seller, product.getId(), "Mouse Pro", "Better mouse",
                    new BigDecimal("150.00"), List.of());

            assertThat(product.getName()).isEqualTo("Mouse Pro");
            assertThat(product.getPrice()).isEqualByComparingTo("150.00");
            assertThat(h.audit.actions()).contains(AuditAction.PRODUCT_UPDATED);
        }

        @Test
        void sellerCannotUpdateAnotherSellersProduct() {
            assertThatThrownBy(() -> h.updateProduct.updateProduct(h.seller(), product.getId(), "Hacked", "Hacked",
                    BigDecimal.ONE, List.of())).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void updatingAnUnknownProductIsReported() {
            assertThatThrownBy(() -> h.updateProduct.updateProduct(seller, "missing", "Name", "Description",
                    BigDecimal.ONE, List.of())).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void publishingMakesTheProductPublishedAndIsAudited() {
            h.publishProduct.publishProduct(seller, product.getId());

            assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
            assertThat(h.audit.last(AuditAction.PRODUCT_PUBLISHED).getDetails())
                    .containsEntry("from", "DRAFT").containsEntry("to", "PUBLISHED");
        }

        @Test
        void publishingTwiceIsRejected() {
            h.publishProduct.publishProduct(seller, product.getId());

            assertThatThrownBy(() -> h.publishProduct.publishProduct(seller, product.getId()))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        void sellerCannotPublishAnotherSellersProduct() {
            assertThatThrownBy(() -> h.publishProduct.publishProduct(h.seller(), product.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void suspendingAPublishedProductIsAudited() {
            h.publishProduct.publishProduct(seller, product.getId());

            h.suspendProduct.suspendProduct(seller, product.getId());

            assertThat(product.getStatus()).isEqualTo(ProductStatus.SUSPENDED);
            assertThat(h.audit.actions()).contains(AuditAction.PRODUCT_SUSPENDED);
        }

        @Test
        void suspendingADraftIsRejected() {
            assertThatThrownBy(() -> h.suspendProduct.suspendProduct(seller, product.getId()))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        void sellerCannotSuspendAnotherSellersProduct() {
            assertThatThrownBy(() -> h.suspendProduct.suspendProduct(h.seller(), product.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void discontinuingIsAudited() {
            h.discontinueProduct.discontinueProduct(seller, product.getId());

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
            assertThat(h.audit.actions()).contains(AuditAction.PRODUCT_DISCONTINUED);
        }

        @Test
        void discontinuingTwiceIsRejected() {
            h.discontinueProduct.discontinueProduct(seller, product.getId());

            assertThatThrownBy(() -> h.discontinueProduct.discontinueProduct(seller, product.getId()))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        void sellerCannotDiscontinueAnotherSellersProduct() {
            assertThatThrownBy(() -> h.discontinueProduct.discontinueProduct(h.seller(), product.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void lifecycleOfAnUnknownProductIsReported() {
            assertThatThrownBy(() -> h.publishProduct.publishProduct(seller, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Catalog {

        @Test
        void catalogContainsOnlyPublishedProducts() {
            PhysicalProduct published = h.physicalProduct(seller, "10.00");
            h.productWithStatus(seller, ProductStatus.DRAFT);

            assertThat(h.consultCatalog.consultCatalog()).containsExactly(published);
        }

        @Test
        void publishedProductIsVisibleById() {
            PhysicalProduct published = h.physicalProduct(seller, "10.00");

            assertThat(h.consultCatalog.consultCatalogProduct(published.getId())).isSameAs(published);
        }

        @Test
        void unpublishedProductIsNotVisibleById() {
            PhysicalProduct suspended = h.productWithStatus(seller, ProductStatus.SUSPENDED);

            assertThatThrownBy(() -> h.consultCatalog.consultCatalogProduct(suspended.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void sellerConsultsItsProductsInAnyStatus() {
            h.physicalProduct(seller, "10.00");
            h.productWithStatus(seller, ProductStatus.DRAFT);

            assertThat(h.consultSellerProducts.consultSellerProducts(seller, seller.getId())).hasSize(2);
        }

        @Test
        void sellerCannotConsultAnotherSellersProducts() {
            assertThatThrownBy(() -> h.consultSellerProducts.consultSellerProducts(h.seller(), seller.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void administratorConsultsTheProductsOfAnySeller() {
            h.physicalProduct(seller, "10.00");

            assertThat(h.consultSellerProducts.consultSellerProducts(h.admin(), seller.getId())).hasSize(1);
        }

        @Test
        void productsOfAnUnknownSellerAreReported() {
            assertThatThrownBy(() -> h.consultSellerProducts.consultSellerProducts(h.admin(), "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
