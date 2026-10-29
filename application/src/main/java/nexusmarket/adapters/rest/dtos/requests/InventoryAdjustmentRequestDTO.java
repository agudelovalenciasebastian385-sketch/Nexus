package nexusmarket.adapters.rest.dtos.requests;

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

    private int quantity;

    private String condition;
}
