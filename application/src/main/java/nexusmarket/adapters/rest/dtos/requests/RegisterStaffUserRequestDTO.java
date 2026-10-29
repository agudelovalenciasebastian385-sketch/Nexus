package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of POST /api/v1/admin/staff-users. role is the SystemRole code LOGISTICS_OPERATOR or SUPERVISOR
 * (any other role is rejected by the domain).
 */
@Data
public class RegisterStaffUserRequestDTO {

    @NotBlank
    private String role;

    @NotBlank
    @Size(max = 40)
    private String identificationNumber;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    /** Trims surrounding spaces so @Email accepts " ana@nexusmarket.com "; the domain lower-cases it. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
