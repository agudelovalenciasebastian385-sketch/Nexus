package nexusmarket.adapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Shipment backed by Spring Data JPA (MySQL).
 *
 * <p>A shipment read on its own receives its fully loaded Order; findByOrder reuses the Order
 * supplied by the caller.
 */
@Repository
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final SpringDataJpaShipmentRepository shipmentRepository;
    private final JpaAggregateLoader loader;

    public ShipmentJpaAdapter(SpringDataJpaShipmentRepository shipmentRepository, JpaAggregateLoader loader) {
        this.shipmentRepository = shipmentRepository;
        this.loader = loader;
    }

    @Override
    public Shipment save(Shipment shipment) {
        shipmentRepository.save(ShipmentJpaMapper.toEntity(shipment));
        return shipment;
    }

    @Override
    public void update(Shipment shipment) {
        shipmentRepository.save(ShipmentJpaMapper.toEntity(shipment));
    }

    @Override
    public Optional<Shipment> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return shipmentRepository.findById(id)
                .map(entity -> loader.toShipment(entity, loader.requireOrder(entity.getOrderId())));
    }

    @Override
    public List<Shipment> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return List.of();
        }
        return shipmentRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(entity -> loader.toShipment(entity, order))
                .toList();
    }

    @Override
    public List<Shipment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        Map<String, Order> orders = new HashMap<>();
        return shipmentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, toExclusive).stream()
                .map(entity -> loader.toShipment(entity,
                        orders.computeIfAbsent(entity.getOrderId(), loader::requireOrder)))
                .toList();
    }
}
