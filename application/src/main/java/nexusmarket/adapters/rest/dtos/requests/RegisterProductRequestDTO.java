package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Body of POST /api/v1/seller/products: the editable data plus the ProductType code
 * (PHYSICAL or DIGITAL), which cannot change afterwards.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterProductRequestDTO extends UpdateProductRequestDTO {

    @NotBlank
    private String type;
}
