package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body of PATCH /api/v1/admin/users/{userId}/status. status is a UserStatus code
 * (ACTIVE, INACTIVE or BLOCKED).
 */
@Data
public class ChangeUserStatusRequestDTO {

    @NotBlank
    private String status;
}
