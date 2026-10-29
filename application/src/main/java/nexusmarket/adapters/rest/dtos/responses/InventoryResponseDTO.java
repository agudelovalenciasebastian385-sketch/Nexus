package nexusmarket.adapters.rest.dtos.responses;

import java.util.List;
import lombok.Data;

/**
 * The inventory of a physical product in a warehouse, with its movement history.
 */
@Data
public class InventoryResponseDTO {

    private String productId;
    private String productName;
    private String warehouseId;
    private String warehouseName;
    private int quantity;
    private int reservedQuantity;
    /** InventoryCondition code. */
    private String condition;
    private List<InventoryMovementResponseDTO> movements;
}
