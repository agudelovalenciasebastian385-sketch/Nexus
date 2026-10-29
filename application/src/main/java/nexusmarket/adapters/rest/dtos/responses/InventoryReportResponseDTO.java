package nexusmarket.adapters.rest.dtos.responses;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Inventory report: stock per warehouse and products below the low-stock threshold.
 */
@Data
public class InventoryReportResponseDTO {

    private int lowStockThreshold;
    private LocalDateTime generatedAt;
    private List<WarehouseStockSummaryResponseDTO> warehouses;
    private List<LowStockProductResponseDTO> lowStockProducts;
}
