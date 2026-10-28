package nexusmarket.adapters.rest.dtos.responses;

import java.util.List;
import lombok.Data;

/**
 * Public view of a buyer. Never includes the password hash or the token version.
 */
@Data
public class BuyerResponseDTO {

    private String id;
    private String identificationNumber;
    private String fullName;
    private String email;
    /** UserStatus code. */
    private String status;
    /** CommercialStatus code. */
    private String commercialStatus;
    private AddressResponseDTO primaryAddress;
    private List<AddressResponseDTO> additionalAddresses;
}
