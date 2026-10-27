package nexusmarket.domain.services.reporting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.RefundRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Consolidates the commercial figures of a period (reporting-services.md -
 * ConsultCommercialReport): orders created per status, total invoiced, total
 * refunded and net revenue. Read-only; the consultation is audited.
 */
@Service
@RequiredArgsConstructor
public class ConsultCommercialReportService {

    private static final String REPORT_TYPE = "COMMERCIAL";

    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public CommercialReport consultCommercialReport(User requestingUser, ReportPeriod period) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR,
                SystemRole.SUPERVISOR);
        if (period == null) {
            throw new InvalidReportPeriodException("A report period is required.");
        }
        LocalDateTime from = period.startDateTime();
        LocalDateTime toExclusive = period.endDateTimeExclusive();

        Map<OrderStatus, Integer> ordersByStatus = new EnumMap<>(OrderStatus.class);
        for (Order order : orderRepositoryPort.findByCreatedAtBetween(from, toExclusive)) {
            ordersByStatus.merge(order.getStatus(), 1, Integer::sum);
        }
        BigDecimal totalInvoiced = invoiceRepositoryPort.findByIssuedAtBetween(from, toExclusive).stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefunded = refundRepositoryPort.findByProcessedAtBetween(from, toExclusive).stream()
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CommercialReport report = new CommercialReport(period, LocalDateTime.now(), ordersByStatus, totalInvoiced,
                totalRefunded);
        registerAuditLogService.execute(requester, AuditAction.REPORT_CONSULTED, AuditEntityType.REPORT, REPORT_TYPE,
                Map.of("reportType", REPORT_TYPE, "from", period.getStartDate().toString(),
                        "to", period.getEndDate().toString()));
        return report;
    }
}
