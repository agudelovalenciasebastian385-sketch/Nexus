package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;

/**
 * Output port: Persistence and queries of {@code Order} and its items.
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    void update(Order order);

    Optional<Order> findById(String id);

    List<Order> findByBuyer(Buyer buyer);
}
