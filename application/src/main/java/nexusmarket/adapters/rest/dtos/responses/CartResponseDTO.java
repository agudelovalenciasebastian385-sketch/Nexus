package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * A buyer's cart with its items and total.
 */
@Data
public class CartResponseDTO {

    private String id;
    private String buyerId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
}
