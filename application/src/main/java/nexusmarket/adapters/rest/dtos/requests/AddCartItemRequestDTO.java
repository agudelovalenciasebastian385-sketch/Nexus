package nexusmarket.adapters.rest.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Body of POST /api/v1/buyer/cart/items. variant is optional (null when the product has none).
 */
@Data
public class AddCartItemRequestDTO {

    @NotBlank
    private String productId;

    @Valid
    private VariantRequestDTO variant;

    @Positive
    @Schema(example = "1")
    private int quantity;
}
