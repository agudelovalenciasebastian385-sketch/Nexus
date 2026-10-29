package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Variant value object in a request body (for example type "color", value "black").
 */
@Data
public class VariantRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String type;

    @NotBlank
    @Size(max = 120)
    private String value;
}
