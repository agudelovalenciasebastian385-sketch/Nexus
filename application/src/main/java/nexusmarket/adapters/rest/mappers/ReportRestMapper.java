package nexusmarket.adapters.rest.mappers;

import java.util.LinkedHashMap;
import java.util.Map;
import nexusmarket.adapters.rest.dtos.responses.CommercialReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.LogisticsReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.LowStockProductResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerPerformanceReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.WarehouseStockSummaryResponseDTO;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.LowStockProduct;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.WarehouseStockSummary;
import nexusmarket.domain.valueobjects.DomainCatalog;

/**
 * Mapper from the read-only report models to their REST DTOs. Catalog keys are exposed as codes.
 */
public final class ReportRestMapper {

    private ReportRestMapper() {
    }

    public static SellerPerformanceReportResponseDTO toResponse(SellerPerformanceReport report) {
        SellerPerformanceReportResponseDTO dto = new SellerPerformanceReportResponseDTO();
        dto.setSellerId(report.getSeller().getId());
        dto.setSellerName(report.getSeller().getFullName());
        dto.setStartDate(report.getPeriod().getStartDate());
        dto.setEndDate(report.getPeriod().getEndDate());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setProductsByStatus(byCode(report.getProductsByStatus()));
        dto.setOrdersWithSellerProducts(report.getOrdersWithSellerProducts());
        dto.setUnitsSold(report.getUnitsSold());
        dto.setAmountSold(report.getAmountSold());
        dto.setApprovedReturns(report.getApprovedReturns());
        dto.setReturnRate(report.getReturnRate());
        return dto;
    }

    public static CommercialReportResponseDTO toResponse(CommercialReport report) {
        CommercialReportResponseDTO dto = new CommercialReportResponseDTO();
        dto.setStartDate(report.getPeriod().getStartDate());
        dto.setEndDate(report.getPeriod().getEndDate());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setOrdersByStatus(byCode(report.getOrdersByStatus()));
        dto.setTotalOrders(report.getTotalOrders());
        dto.setTotalInvoiced(report.getTotalInvoiced());
        dto.setTotalRefunded(report.getTotalRefunded());
        dto.setNetRevenue(report.getNetRevenue());
        return dto;
    }

    public static InventoryReportResponseDTO toResponse(InventoryReport report) {
        InventoryReportResponseDTO dto = new InventoryReportResponseDTO();
        dto.setLowStockThreshold(report.getLowStockThreshold());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setWarehouses(report.getWarehouses().stream().map(ReportRestMapper::toResponse).toList());
        dto.setLowStockProducts(report.getLowStockProducts().stream().map(ReportRestMapper::toResponse).toList());
        return dto;
    }

    public static LogisticsReportResponseDTO toResponse(LogisticsReport report) {
        LogisticsReportResponseDTO dto = new LogisticsReportResponseDTO();
        dto.setStartDate(report.getPeriod().getStartDate());
        dto.setEndDate(report.getPeriod().getEndDate());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setShipmentsByStatus(byCode(report.getShipmentsByStatus()));
        dto.setTotalShipments(report.getTotalShipments());
        dto.setPendingDispatch(report.getPendingDispatch());
        dto.setDelivered(report.getDelivered());
        return dto;
    }

    private static WarehouseStockSummaryResponseDTO toResponse(WarehouseStockSummary summary) {
        WarehouseStockSummaryResponseDTO dto = new WarehouseStockSummaryResponseDTO();
        dto.setWarehouseId(summary.getWarehouse().getId());
        dto.setWarehouseName(summary.getWarehouse().getName());
        dto.setWarehouseType(summary.getWarehouse().getType().getCode());
        dto.setTotalUnits(summary.getTotalUnits());
        dto.setReservedUnits(summary.getReservedUnits());
        dto.setDamagedUnits(summary.getDamagedUnits());
        return dto;
    }

    private static LowStockProductResponseDTO toResponse(LowStockProduct lowStock) {
        LowStockProductResponseDTO dto = new LowStockProductResponseDTO();
        dto.setProductId(lowStock.getProduct().getId());
        dto.setProductName(lowStock.getProduct().getName());
        dto.setSellerId(lowStock.getProduct().getSeller().getId());
        dto.setAvailableUnits(lowStock.getAvailableUnits());
        return dto;
    }

    private static <K extends DomainCatalog> Map<String, Integer> byCode(Map<K, Integer> counts) {
        Map<String, Integer> byCode = new LinkedHashMap<>();
        counts.forEach((key, count) -> byCode.put(key.getCode(), count));
        return byCode;
    }
}
