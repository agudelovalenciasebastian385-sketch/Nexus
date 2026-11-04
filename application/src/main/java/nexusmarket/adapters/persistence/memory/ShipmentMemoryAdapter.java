package nexusmarket.adapters.persistence.memory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Shipments in memory, by creation date like ShipmentJpaAdapter. */
@Repository
@Profile("memory")
public class ShipmentMemoryAdapter implements ShipmentRepositoryPort {

    private final MemoryStore<String, Shipment> shipments = new MemoryStore<>();

    @Override
    public Shipment save(Shipment shipment) {
        shipments.put(shipment.getId(), shipment);
        return shipment;
    }

    @Override
    public void update(Shipment shipment) {
        shipments.put(shipment.getId(), shipment);
    }

    @Override
    public Optional<Shipment> findById(String id) {
        return shipments.get(id);
    }

    @Override
    public List<Shipment> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return List.of();
        }
        return shipments.values().stream()
                .filter(shipment -> shipment.getOrder().getId().equals(order.getId()))
                .sorted(Comparator.comparing(Shipment::getCreatedAt))
                .toList();
    }

    @Override
    public List<Shipment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return shipments.values().stream()
                .filter(shipment -> MemoryStore.inRange(shipment.getCreatedAt(), from, toExclusive)).toList();
    }
}
