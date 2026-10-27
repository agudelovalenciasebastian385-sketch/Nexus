package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.ShipmentStatus;

/**
 * LogisticsReport (read-only).
 *
 * <p>Shipments created in a {@link ReportPeriod} per status, together with the
 * shipments pending dispatch and the delivered ones. It is computed when
 * consulted and never persisted.</p>
 */
public class LogisticsReport {

    private final ReportPeriod period;
    private final LocalDateTime generatedAt;
    /** Shipments created in the period per status; every status is present (0 when there are none). */
    private final Map<ShipmentStatus, Integer> shipmentsByStatus;

    public LogisticsReport(ReportPeriod period, LocalDateTime generatedAt,
                           Map<ShipmentStatus, Integer> shipmentsByStatus) {
        if (period == null) {
            throw new InvalidReportParameterException("Logistics report period is required.");
        }
        if (generatedAt == null) {
            throw new InvalidReportParameterException("Logistics report generatedAt is required.");
        }
        this.period = period;
        this.generatedAt = generatedAt;
        Map<ShipmentStatus, Integer> counts = new EnumMap<>(ShipmentStatus.class);
        for (ShipmentStatus status : ShipmentStatus.values()) {
            int count = shipmentsByStatus == null ? 0 : shipmentsByStatus.getOrDefault(status, 0);
            if (count < 0) {
                throw new InvalidReportParameterException("Shipment count cannot be negative.");
            }
            counts.put(status, count);
        }
        this.shipmentsByStatus = Collections.unmodifiableMap(counts);
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<ShipmentStatus, Integer> getShipmentsByStatus() {
        return shipmentsByStatus;
    }

    /** Number of shipments created in the period. */
    public int getTotalShipments() {
        return shipmentsByStatus.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Shipments not yet dispatched: {@code PREPARING} plus {@code PACKED}. */
    public int getPendingDispatch() {
        return shipmentsByStatus.get(ShipmentStatus.PREPARING) + shipmentsByStatus.get(ShipmentStatus.PACKED);
    }

    /** Shipments in status {@code DELIVERED}. */
    public int getDelivered() {
        return shipmentsByStatus.get(ShipmentStatus.DELIVERED);
    }

    @Override
    public String toString() {
        return "LogisticsReport{period=" + period + ", totalShipments=" + getTotalShipments() + ", pendingDispatch="
                + getPendingDispatch() + ", delivered=" + getDelivered() + "}";
    }
}
