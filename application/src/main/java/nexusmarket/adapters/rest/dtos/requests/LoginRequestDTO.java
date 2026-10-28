package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of POST /api/v1/auth/login.
 */
@Data
public class LoginRequestDTO {

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 72)
    private String password;

    /** Trims surrounding spaces so @Email accepts " juan@gmail.com "; the domain lower-cases it. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
