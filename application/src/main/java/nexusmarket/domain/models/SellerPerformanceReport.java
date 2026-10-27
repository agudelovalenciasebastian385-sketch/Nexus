package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;

/**
 * SellerPerformanceReport (read-only).
 *
 * <p>Commercial performance of one {@link Seller} in a {@link ReportPeriod}:
 * its products per status, the units and amount sold in paid or later orders,
 * the approved returns of those orders and the return rate. It is computed
 * when consulted and never persisted.</p>
 */
public class SellerPerformanceReport {

    private final Seller seller;
    private final ReportPeriod period;
    private final LocalDateTime generatedAt;
    /** Current products of the seller per status; every status is present (0 when there are none). */
    private final Map<ProductStatus, Integer> productsByStatus;
    /** Orders created in the period, paid or later, that contain at least one product of the seller. */
    private final int ordersWithSellerProducts;
    private final int unitsSold;
    private final BigDecimal amountSold;
    /** Approved return requests of {@code ordersWithSellerProducts}. */
    private final int approvedReturns;

    public SellerPerformanceReport(Seller seller, ReportPeriod period, LocalDateTime generatedAt,
                                   Map<ProductStatus, Integer> productsByStatus, int ordersWithSellerProducts,
                                   int unitsSold, BigDecimal amountSold, int approvedReturns) {
        if (seller == null) {
            throw new InvalidReportParameterException("Seller performance report seller is required.");
        }
        if (period == null) {
            throw new InvalidReportParameterException("Seller performance report period is required.");
        }
        if (generatedAt == null) {
            throw new InvalidReportParameterException("Seller performance report generatedAt is required.");
        }
        this.seller = seller;
        this.period = period;
        this.generatedAt = generatedAt;
        Map<ProductStatus, Integer> counts = new EnumMap<>(ProductStatus.class);
        for (ProductStatus status : ProductStatus.values()) {
            counts.put(status, requireCount(productsByStatus == null ? 0 : productsByStatus.getOrDefault(status, 0),
                    "product count"));
        }
        this.productsByStatus = Collections.unmodifiableMap(counts);
        this.ordersWithSellerProducts = requireCount(ordersWithSellerProducts, "ordersWithSellerProducts");
        this.unitsSold = requireCount(unitsSold, "unitsSold");
        if (amountSold == null || amountSold.signum() < 0) {
            throw new InvalidReportParameterException("Seller performance report amountSold must be zero or positive.");
        }
        this.amountSold = amountSold;
        this.approvedReturns = requireCount(approvedReturns, "approvedReturns");
        if (approvedReturns > ordersWithSellerProducts) {
            throw new InvalidReportParameterException(
                    "Approved returns cannot exceed the orders that contain products of the seller.");
        }
    }

    private static int requireCount(int count, String name) {
        if (count < 0) {
            throw new InvalidReportParameterException("Seller performance report " + name + " cannot be negative.");
        }
        return count;
    }

    public Seller getSeller() {
        return seller;
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<ProductStatus, Integer> getProductsByStatus() {
        return productsByStatus;
    }

    public int getOrdersWithSellerProducts() {
        return ordersWithSellerProducts;
    }

    public int getUnitsSold() {
        return unitsSold;
    }

    public BigDecimal getAmountSold() {
        return amountSold;
    }

    public int getApprovedReturns() {
        return approvedReturns;
    }

    /**
     * Approved returns over orders with products of the seller, as a percentage
     * with 2 decimals ({@code HALF_UP}); {@code 0.00} when there are no orders.
     */
    public BigDecimal getReturnRate() {
        if (ordersWithSellerProducts == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(approvedReturns).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(ordersWithSellerProducts), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "SellerPerformanceReport{seller=" + seller.getId() + ", period=" + period + ", unitsSold=" + unitsSold
                + ", amountSold=" + amountSold + ", approvedReturns=" + approvedReturns + ", returnRate="
                + getReturnRate() + "}";
    }
}
