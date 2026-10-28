package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.valueobjects.MovementType;

/**
 * Bidirectional mapper between the InventoryMovement Domain Model and its JPA entity.
 *
 * <p>The owning Inventory is identified by (productId, warehouseId), supplied by the adapter.
 */
public final class InventoryMovementJpaMapper {

    private InventoryMovementJpaMapper() {
    }

    public static InventoryMovementJpaEntity toEntity(InventoryMovement domain, String productId,
                                                      String warehouseId) {
        if (domain == null) {
            return null;
        }
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setProductId(productId);
        entity.setWarehouseId(warehouseId);
        entity.setMovementType(domain.getType().getCode());
        entity.setQuantity(domain.getQuantity());
        entity.setMovementDate(domain.getDate());
        return entity;
    }

    public static InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryMovement(MovementType.fromCode(entity.getMovementType()), entity.getQuantity(),
                entity.getMovementDate());
    }
}
