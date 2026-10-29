package nexusmarket.adapters.rest.dtos.responses;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * A shipment of an order (one per origin warehouse).
 */
@Data
public class ShipmentResponseDTO {

    private String id;
    private String orderId;
    private String originWarehouseId;
    private String originWarehouseName;
    private AddressResponseDTO deliveryAddress;
    private String logisticsOperatorId;
    /** ShipmentStatus code. */
    private String status;
    private LocalDateTime createdAt;
}
