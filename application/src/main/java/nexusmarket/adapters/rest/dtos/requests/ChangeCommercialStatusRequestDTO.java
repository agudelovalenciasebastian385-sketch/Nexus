package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body of PATCH /api/v1/admin/buyers/{buyerId}/commercial-status. commercialStatus is a
 * CommercialStatus code.
 */
@Data
public class ChangeCommercialStatusRequestDTO {

    @NotBlank
    private String commercialStatus;
}
