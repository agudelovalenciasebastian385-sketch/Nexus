package nexusmarket.adapters.rest.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body of PATCH /api/v1/admin/users/{userId}/status. status is a UserStatus code
 * (ACTIVE, INACTIVE or BLOCKED).
 */
@Data
public class ChangeUserStatusRequestDTO {

    @NotBlank
    @Schema(example = "ACTIVE")
    private String status;
}
