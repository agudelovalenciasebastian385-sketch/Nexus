package nexusmarket.adapters.rest.dtos.responses;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * One movement of an inventory history.
 */
@Data
public class InventoryMovementResponseDTO {

    /** MovementType code. */
    private String type;
    private int quantity;
    private LocalDateTime date;
}
