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
 * JPA persistence entity (repository DTO) for the Refund aggregate.
 *
 * <p>processed_at is set only once the refund is PROCESSED, matching the domain rule.
 */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class RefundJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Refund.returnRequest reference. */
    @Column(name = "return_request_id", nullable = false, unique = true, length = 36)
    private String returnRequestId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** RefundStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
