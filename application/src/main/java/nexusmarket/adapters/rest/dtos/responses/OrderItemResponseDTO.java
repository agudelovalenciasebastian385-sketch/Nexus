package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import lombok.Data;

/**
 * One line of an order. originWarehouseId is null for digital products.
 */
@Data
public class OrderItemResponseDTO {

    private String productId;
    private String productName;
    /** ProductType code. */
    private String productType;
    private VariantResponseDTO variant;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String originWarehouseId;
}
