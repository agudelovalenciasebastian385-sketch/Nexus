package nexusmarket.adapters.rest.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body of PATCH /api/v1/admin/buyers/{buyerId}/commercial-status. commercialStatus is a
 * CommercialStatus code.
 */
@Data
public class ChangeCommercialStatusRequestDTO {

    @NotBlank
    @Schema(example = "ENABLED")
    private String commercialStatus;
}
