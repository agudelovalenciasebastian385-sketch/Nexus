package nexusmarket.adapters.rest.mappers;

import java.util.LinkedHashMap;
import java.util.Map;
import nexusmarket.adapters.rest.dtos.responses.SellerPerformanceReportResponseDTO;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.valueobjects.ProductStatus;

/**
 * Mapper from the read-only report models to their REST DTOs.
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

    private static Map<String, Integer> byCode(Map<ProductStatus, Integer> counts) {
        Map<String, Integer> byCode = new LinkedHashMap<>();
        counts.forEach((status, count) -> byCode.put(status.getCode(), count));
        return byCode;
    }
}
