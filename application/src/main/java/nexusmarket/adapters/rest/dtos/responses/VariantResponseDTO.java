package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Variant value object in a response body.
 */
@Data
public class VariantResponseDTO {

    private String type;
    private String value;
}
