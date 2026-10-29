package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * A warehouse. sellerId is null for a Marketplace warehouse.
 */
@Data
public class WarehouseResponseDTO {

    private String id;
    private String name;
    /** WarehouseType code. */
    private String type;
    private AddressResponseDTO location;
    private String sellerId;
}
