package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Public view of a seller. Never includes the password hash or the token version; warehouses and
 * products are consulted through their own endpoints.
 */
@Data
public class SellerResponseDTO {

    private String id;
    private String identificationNumber;
    private String fullName;
    private String email;
    /** UserStatus code. */
    private String status;
}
