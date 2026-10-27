package nexusmarket.domain.services.reporting;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.ShipmentStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Consolidates the shipments created in a period per status
 * (reporting-services.md - ConsultLogisticsReport). Read-only; the
 * consultation is audited.
 */
@Service
@RequiredArgsConstructor
public class ConsultLogisticsReportService {

    private static final String REPORT_TYPE = "LOGISTICS";

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public LogisticsReport consultLogisticsReport(User requestingUser, ReportPeriod period) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR,
                SystemRole.SUPERVISOR);
        if (period == null) {
            throw new InvalidReportPeriodException("A report period is required.");
        }

        Map<ShipmentStatus, Integer> shipmentsByStatus = new EnumMap<>(ShipmentStatus.class);
        for (Shipment shipment : shipmentRepositoryPort.findByCreatedAtBetween(period.startDateTime(),
                period.endDateTimeExclusive())) {
            shipmentsByStatus.merge(shipment.getStatus(), 1, Integer::sum);
        }

        LogisticsReport report = new LogisticsReport(period, LocalDateTime.now(), shipmentsByStatus);
        registerAuditLogService.execute(requester, AuditAction.REPORT_CONSULTED, AuditEntityType.REPORT, REPORT_TYPE,
                Map.of("reportType", REPORT_TYPE, "from", period.getStartDate().toString(),
                        "to", period.getEndDate().toString()));
        return report;
    }
}
