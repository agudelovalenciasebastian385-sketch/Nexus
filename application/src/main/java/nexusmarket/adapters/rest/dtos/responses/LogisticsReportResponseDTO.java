package nexusmarket.adapters.rest.dtos.responses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * Logistics report for an inclusive period of days.
 */
@Data
public class LogisticsReportResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    /** Shipments created in the period per ShipmentStatus code. */
    private Map<String, Integer> shipmentsByStatus;
    private int totalShipments;
    private int pendingDispatch;
    private int delivered;
}
