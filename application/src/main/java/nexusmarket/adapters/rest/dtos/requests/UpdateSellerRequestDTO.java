package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of PATCH /api/v1/seller/profile. Only the full name can change.
 */
@Data
public class UpdateSellerRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String fullName;
}
