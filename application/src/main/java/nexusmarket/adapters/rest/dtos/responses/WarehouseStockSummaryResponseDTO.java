package nexusmarket.adapters.rest.dtos.responses;

import lombok.Data;

/**
 * Stock totals of one warehouse in the inventory report.
 */
@Data
public class WarehouseStockSummaryResponseDTO {

    private String warehouseId;
    private String warehouseName;
    /** WarehouseType code. */
    private String warehouseType;
    private int totalUnits;
    private int reservedUnits;
    private int damagedUnits;
}
