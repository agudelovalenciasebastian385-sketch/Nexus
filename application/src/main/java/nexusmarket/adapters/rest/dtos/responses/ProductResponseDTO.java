package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * Public view of a product. The seller is shown by id and name only.
 */
@Data
public class ProductResponseDTO {

    private String id;
    /** ProductType code (PHYSICAL or DIGITAL). */
    private String type;
    private String name;
    private String description;
    private BigDecimal price;
    /** ProductStatus code. */
    private String status;
    private String sellerId;
    private String sellerName;
    private List<VariantResponseDTO> variants;
}
