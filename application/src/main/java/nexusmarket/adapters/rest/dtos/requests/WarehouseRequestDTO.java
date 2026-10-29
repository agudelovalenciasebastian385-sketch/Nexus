package nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body to register or update a warehouse. The type is not sent: it is fixed by the role
 * (a seller registers SELLER warehouses).
 */
@Data
public class WarehouseRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    @Valid
    private AddressRequestDTO location;
}
