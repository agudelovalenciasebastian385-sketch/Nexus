package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.ShipmentStatus;

/**
 * Bidirectional mapper between the Shipment Domain Model and its JPA entity.
 *
 * <p>Order, origin warehouse and logistics operator are stored as reference identifiers; the
 * adapter supplies the resolved objects when reading. createdAt is restored as stored.
 */
public final class ShipmentJpaMapper {

    private ShipmentJpaMapper() {
    }

    public static ShipmentJpaEntity toEntity(Shipment domain) {
        if (domain == null) {
            return null;
        }
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrder().getId());
        entity.setOriginWarehouseId(domain.getOriginWarehouse().getId());
        entity.setDeliveryAddress(AddressJpaMapper.toEmbeddable(domain.getDeliveryAddress()));
        entity.setLogisticsOperatorId(domain.getLogisticsOperator().getId());
        entity.setStatus(domain.getStatus().getCode());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static Shipment toDomain(ShipmentJpaEntity entity, Order order, Warehouse originWarehouse,
                                    LogisticsOperator logisticsOperator) {
        if (entity == null) {
            return null;
        }
        return new Shipment(entity.getId(), order, originWarehouse,
                AddressJpaMapper.toDomain(entity.getDeliveryAddress()), logisticsOperator,
                ShipmentStatus.fromCode(entity.getStatus()), entity.getCreatedAt());
    }
}
