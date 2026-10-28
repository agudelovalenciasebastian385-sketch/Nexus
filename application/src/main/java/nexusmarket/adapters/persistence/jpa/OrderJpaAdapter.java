package nexusmarket.adapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.OrderItemJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.OrderJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaOrderItemRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output persistence adapter for Order backed by Spring Data JPA (MySQL).
 *
 * <p>Order items are immutable once the order exists, so they are written only on the first
 * save; update writes the order row (status). Shipments and the invoice are written by their own
 * adapters. A loaded Order carries its shipments but never its invoice (see OrderJpaMapper).
 */
@Repository
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataJpaOrderRepository orderRepository;
    private final SpringDataJpaOrderItemRepository orderItemRepository;
    private final JpaAggregateLoader loader;

    public OrderJpaAdapter(SpringDataJpaOrderRepository orderRepository,
                           SpringDataJpaOrderItemRepository orderItemRepository,
                           JpaAggregateLoader loader) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.loader = loader;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        orderRepository.save(OrderJpaMapper.toEntity(order));
        if (!orderItemRepository.existsByOrderId(order.getId())) {
            for (OrderItem item : order.getItems()) {
                orderItemRepository.save(OrderItemJpaMapper.toEntity(item, order.getId()));
            }
        }
        return order;
    }

    @Override
    public void update(Order order) {
        orderRepository.save(OrderJpaMapper.toEntity(order));
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return orderRepository.findById(id).map(loader::toOrder);
    }

    @Override
    public List<Order> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getId() == null) {
            return List.of();
        }
        return orderRepository.findByBuyerIdOrderByCreatedAtAsc(buyer.getId()).stream()
                .map(entity -> loader.toOrder(entity, buyer))
                .toList();
    }

    @Override
    public List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, toExclusive).stream()
                .map(loader::toOrder)
                .toList();
    }
}
