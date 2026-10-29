package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body of POST /api/v1/admin/sellers: the seller and its first (SELLER) warehouse.
 */
@Data
public class RegisterSellerRequestDTO {

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

    @NotBlank
    @Size(max = 150)
    private String warehouseName;

    @NotNull
    @Valid
    private AddressRequestDTO warehouseLocation;

    /** Trims surrounding spaces so @Email accepts " store@gmail.com "; the domain lower-cases it. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
