package nexusmarket.domain.services.reporting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.ReturnStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Consolidates the performance of a seller in a period
 * (reporting-services.md - ConsultSellerPerformanceReport). A seller only
 * consults its own report. Read-only; the consultation is audited.
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerPerformanceReportService {

    private static final String REPORT_TYPE = "SELLER_PERFORMANCE";
    /** Orders whose payment was validated: they count as sold. */
    private static final Set<OrderStatus> SOLD_STATUSES = EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPED,
            OrderStatus.DELIVERED);

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId,
                                                                  ReportPeriod period) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR,
                SystemRole.SUPERVISOR, SystemRole.SELLER);
        if (period == null) {
            throw new InvalidReportPeriodException("A report period is required.");
        }
        Seller seller = sellerRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        validateSellerOwnershipService.execute(requester, seller);

        Map<ProductStatus, Integer> productsByStatus = new EnumMap<>(ProductStatus.class);
        for (Product product : productRepositoryPort.findBySeller(seller)) {
            productsByStatus.merge(product.getStatus(), 1, Integer::sum);
        }

        int ordersWithSellerProducts = 0;
        int unitsSold = 0;
        BigDecimal amountSold = BigDecimal.ZERO;
        int approvedReturns = 0;
        for (Order order : orderRepositoryPort.findByCreatedAtBetween(period.startDateTime(),
                period.endDateTimeExclusive())) {
            if (!SOLD_STATUSES.contains(order.getStatus())) {
                continue;
            }
            boolean containsSellerProducts = false;
            for (OrderItem item : order.getItems()) {
                if (item.getProduct().getSeller().getId().equals(seller.getId())) {
                    containsSellerProducts = true;
                    unitsSold += item.getQuantity();
                    amountSold = amountSold.add(item.subtotal());
                }
            }
            if (containsSellerProducts) {
                ordersWithSellerProducts++;
                approvedReturns += (int) returnRequestRepositoryPort.findByOrder(order).stream()
                        .filter(returnRequest -> returnRequest.getStatus() == ReturnStatus.APPROVED)
                        .count();
            }
        }

        SellerPerformanceReport report = new SellerPerformanceReport(seller, period, LocalDateTime.now(),
                productsByStatus, ordersWithSellerProducts, unitsSold, amountSold, approvedReturns);
        registerAuditLogService.execute(requester, AuditAction.REPORT_CONSULTED, AuditEntityType.REPORT, REPORT_TYPE,
                Map.of("reportType", REPORT_TYPE, "sellerId", seller.getId(),
                        "from", period.getStartDate().toString(), "to", period.getEndDate().toString()));
        return report;
    }
}
