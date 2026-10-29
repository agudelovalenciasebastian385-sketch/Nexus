package nexusmarket.domain.services.support;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.OrderRepositoryPort;

/** In-memory OrderRepositoryPort. The date range is half-open: [from, toExclusive). */
public class InMemoryOrderRepository implements OrderRepositoryPort {

    private final Map<String, Order> orders = new LinkedHashMap<>();

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
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findByBuyer(Buyer buyer) {
        return orders.values().stream().filter(order -> order.getBuyer().getId().equals(buyer.getId())).toList();
    }

    @Override
    public List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return orders.values().stream()
                .filter(order -> !order.getCreatedAt().isBefore(from) && order.getCreatedAt().isBefore(toExclusive))
                .toList();
    }

    public List<Order> all() {
        return List.copyOf(orders.values());
    }
}
