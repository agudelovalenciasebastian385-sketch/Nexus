package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Invoice aggregate.
 *
 * <p>An order generates exactly one invoice, so order_id is unique.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Invoice.order reference. */
    @Column(name = "order_id", nullable = false, unique = true, length = 36)
    private String orderId;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
}
