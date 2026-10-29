package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Body of PUT /api/v1/buyer/profile. additionalAddresses replaces the current list (empty by default).
 */
@Data
public class UpdateBuyerProfileRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotNull
    @Valid
    private AddressRequestDTO primaryAddress;

    @Valid
    private List<AddressRequestDTO> additionalAddresses = new ArrayList<>();
}
