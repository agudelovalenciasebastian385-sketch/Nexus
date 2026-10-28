package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Warehouse;

/**
 * Bidirectional mapper between the OrderItem Domain Model and its JPA entity.
 *
 * <p>Product and origin warehouse are stored as reference identifiers; the adapter supplies the
 * resolved objects when reading (no warehouse for a digital product).
 */
public final class OrderItemJpaMapper {

    private OrderItemJpaMapper() {
    }

    public static OrderItemJpaEntity toEntity(OrderItem domain, String orderId) {
        if (domain == null) {
            return null;
        }
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setOrderId(orderId);
        entity.setProductId(domain.getProduct().getId());
        entity.setVariant(VariantJpaMapper.toEmbeddable(domain.getVariant()));
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        entity.setOriginWarehouseId(domain.getOriginWarehouse() != null ? domain.getOriginWarehouse().getId() : null);
        return entity;
    }

    public static OrderItem toDomain(OrderItemJpaEntity entity, Product product, Warehouse originWarehouse) {
        if (entity == null) {
            return null;
        }
        return new OrderItem(product, VariantJpaMapper.toDomain(entity.getVariant()), entity.getQuantity(),
                entity.getUnitPrice(), originWarehouse);
    }
}
