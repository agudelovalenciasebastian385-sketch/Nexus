package nexusmarket.adapters.persistence.jpa.mappers;

import java.util.ArrayList;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.VariantEmbeddable;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Bidirectional mapper between the Product hierarchy and the single products table.
 *
 * <p>The product_type column selects PhysicalProduct or DigitalProduct. The seller is stored as
 * a reference identifier; the resolved Seller is supplied by the adapter when reading.
 */
public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static ProductJpaEntity toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(domain.getId());
        entity.setProductType(domain.getType().getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice());
        entity.setSellerId(domain.getSeller().getId());
        entity.setStatus(domain.getStatus().getCode());
        List<VariantEmbeddable> variants = new ArrayList<>();
        for (Variant variant : domain.getVariants()) {
            variants.add(VariantJpaMapper.toEmbeddable(variant));
        }
        entity.setVariants(variants);
        return entity;
    }

    public static Product toDomain(ProductJpaEntity entity, Seller seller) {
        if (entity == null) {
            return null;
        }
        List<Variant> variants = new ArrayList<>();
        if (entity.getVariants() != null) {
            for (VariantEmbeddable embeddable : entity.getVariants()) {
                variants.add(VariantJpaMapper.toDomain(embeddable));
            }
        }
        ProductStatus status = ProductStatus.fromCode(entity.getStatus());
        return switch (ProductType.fromCode(entity.getProductType())) {
            case PHYSICAL -> new PhysicalProduct(entity.getId(), entity.getName(), entity.getDescription(),
                    entity.getPrice(), seller, variants, status);
            case DIGITAL -> new DigitalProduct(entity.getId(), entity.getName(), entity.getDescription(),
                    entity.getPrice(), seller, variants, status);
        };
    }
}
