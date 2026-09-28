package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;

/**
 * Output port: Persistence and queries of {@code Invoice}.
 */
public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByOrder(Order order);

    boolean existsByOrder(Order order);
}
