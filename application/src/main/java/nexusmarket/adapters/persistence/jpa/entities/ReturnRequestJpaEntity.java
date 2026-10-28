package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the ReturnRequest aggregate.
 */
@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequestJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** ReturnRequest.order reference. */
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    /** ReturnStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;
}
