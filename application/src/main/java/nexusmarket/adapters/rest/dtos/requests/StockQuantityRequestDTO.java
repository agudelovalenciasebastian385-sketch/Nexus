package nexusmarket.adapters.rest.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Body with a positive stock quantity (for example an inbound movement).
 */
@Data
public class StockQuantityRequestDTO {

    @Positive
    @Schema(example = "1")
    private int quantity;
}
