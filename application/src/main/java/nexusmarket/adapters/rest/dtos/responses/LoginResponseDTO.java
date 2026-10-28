package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Response of POST /api/v1/auth/login.
 */
@Data
public class LoginResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    /** Token lifetime in seconds. */
    private Long expiresIn;
    private UserResponseDTO user;
}
