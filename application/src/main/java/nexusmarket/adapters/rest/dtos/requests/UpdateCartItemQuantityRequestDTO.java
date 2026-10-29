package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Body of PATCH /api/v1/buyer/cart/items/{productId}. variant identifies the line together with the
 * product (null when the line has no variant).
 */
@Data
public class UpdateCartItemQuantityRequestDTO {

    @Valid
    private VariantRequestDTO variant;

    @Positive
    private int quantity;
}
