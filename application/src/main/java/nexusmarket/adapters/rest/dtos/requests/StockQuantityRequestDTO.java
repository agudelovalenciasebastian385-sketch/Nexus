package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Body with a positive stock quantity (for example an inbound movement).
 */
@Data
public class StockQuantityRequestDTO {

    @Positive
    private int quantity;
}
