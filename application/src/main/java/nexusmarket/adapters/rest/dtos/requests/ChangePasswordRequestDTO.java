package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of PUT /api/v1/{role}/account/password. Changing the password invalidates the current token.
 */
@Data
public class ChangePasswordRequestDTO {

    @NotBlank
    @Size(max = 72)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
