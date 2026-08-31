package nexusmarket.domain.models;

import java.util.List;

import nexusmarket.domain.valueobjects.ProductStatus;

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

    public DigitalProduct(String id, String name, String description, Seller seller,
                          List<String> variants, ProductStatus status) {
        super(id, name, description, seller, variants, status);
    }
}
