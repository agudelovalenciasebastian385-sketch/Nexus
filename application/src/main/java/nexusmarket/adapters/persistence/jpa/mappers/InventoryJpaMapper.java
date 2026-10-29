package nexusmarket.adapters.persistence.jpa.mappers;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaId;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.InventoryCondition;

/**
 * Bidirectional mapper between the Inventory Domain Model and its JPA entity.
 *
 * <p>Product and warehouse form the composite key; the resolved product, warehouse and the
 * movement history are supplied by the adapter when reading.
 */
public final class InventoryJpaMapper {

    private InventoryJpaMapper() {
    }

    public static InventoryJpaEntity toEntity(Inventory domain) {
        if (domain == null) {
            return null;
        }
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setId(idOf(domain));
        entity.setQuantity(domain.getQuantity());
        entity.setReservedQuantity(domain.getReservedQuantity());
        entity.setCondition(domain.getCondition().getCode());
        return entity;
    }

    /**
     * Copies the mutable domain state onto the managed entity loaded in the current transaction, so
     * Hibernate keeps the version read at the start of the use case (optimistic locking).
     */
    public static InventoryJpaEntity copyToEntity(Inventory domain, InventoryJpaEntity entity) {
        entity.setQuantity(domain.getQuantity());
        entity.setReservedQuantity(domain.getReservedQuantity());
        entity.setCondition(domain.getCondition().getCode());
        return entity;
    }

    public static Inventory toDomain(InventoryJpaEntity entity, PhysicalProduct product, Warehouse warehouse,
                                     List<InventoryMovement> movements) {
        if (entity == null) {
            return null;
        }
        return new Inventory(product, warehouse, entity.getQuantity(), entity.getReservedQuantity(),
                InventoryCondition.fromCode(entity.getCondition()), movements);
    }

    public static InventoryJpaId idOf(Inventory domain) {
        return new InventoryJpaId(domain.getProduct().getId(), domain.getWarehouse().getId());
    }
}
