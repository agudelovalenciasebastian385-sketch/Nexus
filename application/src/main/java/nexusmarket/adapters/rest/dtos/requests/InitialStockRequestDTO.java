package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Body of POST /api/v1/{role}/inventories: creates the inventory of a product in a warehouse.
 */
@Data
public class InitialStockRequestDTO {

    @NotBlank
    private String productId;

    @NotBlank
    private String warehouseId;

    @Positive
    private int quantity;
}
