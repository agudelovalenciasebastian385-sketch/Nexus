package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Public view of a user of any role. Never includes the password hash or the token version.
 */
@Data
public class UserResponseDTO {

    private String id;
    private String identificationNumber;
    private String fullName;
    private String email;
    /** SystemRole code. */
    private String role;
    /** UserStatus code. */
    private String status;
}
