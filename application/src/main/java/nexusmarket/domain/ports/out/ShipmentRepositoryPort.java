package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;

/**
 * Output port: Persistence and queries of {@code Shipment}.
 */
public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);

    void update(Shipment shipment);

    Optional<Shipment> findById(String id);

    List<Shipment> findByOrder(Order order);
}
