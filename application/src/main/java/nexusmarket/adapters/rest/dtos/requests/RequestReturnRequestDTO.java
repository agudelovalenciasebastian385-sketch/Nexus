package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of POST /api/v1/buyer/orders/{orderId}/returns.
 */
@Data
public class RequestReturnRequestDTO {

    @NotBlank
    @Size(max = 1000)
    private String reason;
}
