package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.List;

import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;

/**
 * DigitalProduct.
 *
 * <p>Represents an intangible good delivered immediately after payment
 * confirmation, requiring no physical inventory or logistics process.</p>
 *
 * <p>It holds no relationship with {@link Warehouse} or {@link Inventory} and
 * never generates {@link Shipment} instances.</p>
 */
public class DigitalProduct extends Product {

    public DigitalProduct(String id, String name, String description, BigDecimal price, Seller seller,
                        List<Variant> variants, ProductStatus status) {
        super(id, name, description, price, seller, variants, status);
    }

    @Override
    public ProductType getType() {
        return ProductType.DIGITAL;
    }
}
