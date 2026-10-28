package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Bidirectional mapper between the Warehouse Domain Model and its JPA entity.
 *
 * <p>The seller is stored as a reference identifier; the resolved Seller is supplied by the
 * adapter when reading (null for a Marketplace warehouse).
 */
public final class WarehouseJpaMapper {

    private WarehouseJpaMapper() {
    }

    public static WarehouseJpaEntity toEntity(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setLocation(AddressJpaMapper.toEmbeddable(domain.getLocation()));
        entity.setType(domain.getType().getCode());
        entity.setSellerId(domain.getSeller() != null ? domain.getSeller().getId() : null);
        return entity;
    }

    public static Warehouse toDomain(WarehouseJpaEntity entity, Seller seller) {
        if (entity == null) {
            return null;
        }
        return new Warehouse(entity.getId(), entity.getName(), AddressJpaMapper.toDomain(entity.getLocation()),
                WarehouseType.fromCode(entity.getType()), seller);
    }
}
