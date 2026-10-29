package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Response of POST /api/v1/admin/sellers: the new seller and its first warehouse. A seller loaded later
 * does not carry its warehouses, so the id of the first one is returned here.
 */
@Data
public class SellerRegistrationResponseDTO {

    private SellerResponseDTO seller;
    private WarehouseResponseDTO firstWarehouse;
}
