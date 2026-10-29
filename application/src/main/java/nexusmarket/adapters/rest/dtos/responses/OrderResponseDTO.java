package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * An order with its items and shipments. The invoice is consulted separately
 * (GET .../orders/{orderId}/invoice): a loaded order never carries it.
 */
@Data
public class OrderResponseDTO {

    private String id;
    private String buyerId;
    /** OrderStatus code. */
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDTO> items;
    private List<ShipmentResponseDTO> shipments;
}
