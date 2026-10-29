package nexusmarket.domain.services.support;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;

/** In-memory ShipmentRepositoryPort. The date range is half-open: [from, toExclusive). */
public class InMemoryShipmentRepository implements ShipmentRepositoryPort {

    private final Map<String, Shipment> shipments = new LinkedHashMap<>();

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
        return Optional.ofNullable(shipments.get(id));
    }

    @Override
    public List<Shipment> findByOrder(Order order) {
        return shipments.values().stream().filter(shipment -> shipment.getOrder().getId().equals(order.getId())).toList();
    }

    @Override
    public List<Shipment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return shipments.values().stream()
                .filter(shipment -> !shipment.getCreatedAt().isBefore(from) && shipment.getCreatedAt().isBefore(toExclusive))
                .toList();
    }
}
