package nexusmarket.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * Commercial report for an inclusive period of days.
 */
@Data
public class CommercialReportResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    /** Orders created in the period per OrderStatus code. */
    private Map<String, Integer> ordersByStatus;
    private int totalOrders;
    private BigDecimal totalInvoiced;
    private BigDecimal totalRefunded;
    private BigDecimal netRevenue;
}
