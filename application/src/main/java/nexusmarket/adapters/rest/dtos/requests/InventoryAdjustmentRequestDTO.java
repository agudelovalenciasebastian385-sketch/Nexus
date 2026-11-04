package nexusmarket.adapters.rest.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Body of POST /api/v1/logistics/inventories/{productId}/{warehouseId}/adjustments.
 *
 * <p>quantity is a signed correction of the available stock (0 = no correction); condition is an
 * optional InventoryCondition code. At least one of them must change something, and the stock can never
 * become negative: both rules are enforced by the domain (400).
 */
@Data
public class InventoryAdjustmentRequestDTO {

    @Schema(example = "1")
    private int quantity;

    @Schema(example = "AVAILABLE")
    private String condition;
}
