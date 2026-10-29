package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * The refund of an approved return request. processedAt is null until it is PROCESSED.
 */
@Data
public class RefundResponseDTO {

    private String id;
    private String returnRequestId;
    private BigDecimal amount;
    /** RefundStatus code. */
    private String status;
    private LocalDateTime processedAt;
}
