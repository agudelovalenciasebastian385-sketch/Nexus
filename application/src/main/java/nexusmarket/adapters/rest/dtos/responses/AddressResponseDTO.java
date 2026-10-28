package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Address value object in a response body.
 */
@Data
public class AddressResponseDTO {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
}
