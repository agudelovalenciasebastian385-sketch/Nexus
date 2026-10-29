package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import lombok.Data;

/**
 * One line of a cart. productStatus lets the client warn about items whose product is no longer
 * published (they stay in the cart and are rejected at checkout).
 */
@Data
public class CartItemResponseDTO {

    private String productId;
    private String productName;
    /** ProductStatus code. */
    private String productStatus;
    private VariantResponseDTO variant;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
