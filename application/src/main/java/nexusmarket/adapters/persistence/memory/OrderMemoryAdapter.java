package nexusmarket.adapters.persistence.memory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Orders in memory, by creation date like OrderJpaAdapter. */
@Repository
@Profile("memory")
public class OrderMemoryAdapter implements OrderRepositoryPort {

    private final MemoryStore<String, Order> orders = new MemoryStore<>();

    @Override
    public Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public void update(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orders.get(id);
    }

    @Override
    public List<Order> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getId() == null) {
            return List.of();
        }
        return orders.values().stream()
                .filter(order -> order.getBuyer().getId().equals(buyer.getId()))
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();
    }

    @Override
    public List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return orders.values().stream()
                .filter(order -> MemoryStore.inRange(order.getCreatedAt(), from, toExclusive)).toList();
    }
}
