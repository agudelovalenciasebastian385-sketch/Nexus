package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * A physical product whose available units are below the threshold of the inventory report.
 */
@Data
public class LowStockProductResponseDTO {

    private String productId;
    private String productName;
    private String sellerId;
    private int availableUnits;
}
