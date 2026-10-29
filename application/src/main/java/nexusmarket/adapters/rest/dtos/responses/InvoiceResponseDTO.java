package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * The invoice of an order.
 */
@Data
public class InvoiceResponseDTO {

    private String id;
    private String orderId;
    private BigDecimal totalAmount;
    private LocalDateTime issuedAt;
}
