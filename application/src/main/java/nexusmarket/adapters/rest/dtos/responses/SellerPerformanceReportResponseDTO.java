package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * Seller performance report for an inclusive period of days.
 */
@Data
public class SellerPerformanceReportResponseDTO {

    private String sellerId;
    private String sellerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    /** Current products of the seller per ProductStatus code. */
    private Map<String, Integer> productsByStatus;
    private int ordersWithSellerProducts;
    private int unitsSold;
    private BigDecimal amountSold;
    private int approvedReturns;
    /** Approved returns / orders with seller products x 100. */
    private BigDecimal returnRate;
}
