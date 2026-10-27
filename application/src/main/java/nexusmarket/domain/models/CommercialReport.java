package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;

/**
 * CommercialReport (read-only).
 *
 * <p>Consolidated commercial figures of a {@link ReportPeriod}: orders created
 * per status, total invoiced, total refunded and net revenue. It is computed
 * when consulted and never persisted.</p>
 */
public class CommercialReport {

    private final ReportPeriod period;
    private final LocalDateTime generatedAt;
    /** Orders created in the period per status; every status is present (0 when there are none). */
    private final Map<OrderStatus, Integer> ordersByStatus;
    /** Sum of the invoices issued in the period. */
    private final BigDecimal totalInvoiced;
    /** Sum of the refunds processed in the period. */
    private final BigDecimal totalRefunded;

    public CommercialReport(ReportPeriod period, LocalDateTime generatedAt, Map<OrderStatus, Integer> ordersByStatus,
                            BigDecimal totalInvoiced, BigDecimal totalRefunded) {
        if (period == null) {
            throw new InvalidReportParameterException("Commercial report period is required.");
        }
        if (generatedAt == null) {
            throw new InvalidReportParameterException("Commercial report generatedAt is required.");
        }
        this.period = period;
        this.generatedAt = generatedAt;
        Map<OrderStatus, Integer> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            int count = ordersByStatus == null ? 0 : ordersByStatus.getOrDefault(status, 0);
            if (count < 0) {
                throw new InvalidReportParameterException("Order count cannot be negative.");
            }
            counts.put(status, count);
        }
        this.ordersByStatus = Collections.unmodifiableMap(counts);
        this.totalInvoiced = requireAmount(totalInvoiced, "totalInvoiced");
        this.totalRefunded = requireAmount(totalRefunded, "totalRefunded");
    }

    private static BigDecimal requireAmount(BigDecimal amount, String name) {
        if (amount == null || amount.signum() < 0) {
            throw new InvalidReportParameterException("Commercial report " + name + " must be zero or positive.");
        }
        return amount;
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<OrderStatus, Integer> getOrdersByStatus() {
        return ordersByStatus;
    }

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public BigDecimal getTotalRefunded() {
        return totalRefunded;
    }

    /** Number of orders created in the period. */
    public int getTotalOrders() {
        return ordersByStatus.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Total invoiced minus total refunded. */
    public BigDecimal getNetRevenue() {
        return totalInvoiced.subtract(totalRefunded);
    }

    @Override
    public String toString() {
        return "CommercialReport{period=" + period + ", totalOrders=" + getTotalOrders() + ", totalInvoiced="
                + totalInvoiced + ", totalRefunded=" + totalRefunded + ", netRevenue=" + getNetRevenue() + "}";
    }
}
