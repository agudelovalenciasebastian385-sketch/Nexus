package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * A return request of an order.
 */
@Data
public class ReturnRequestResponseDTO {

    private String id;
    private String orderId;
    private String reason;
    /** ReturnStatus code. */
    private String status;
}
