package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;

/**
 * Mapper from ReturnRequest and Refund to their REST DTOs.
 */
public final class ReturnRestMapper {

    private ReturnRestMapper() {
    }

    public static ReturnRequestResponseDTO toResponse(ReturnRequest returnRequest) {
        ReturnRequestResponseDTO dto = new ReturnRequestResponseDTO();
        dto.setId(returnRequest.getId());
        dto.setOrderId(returnRequest.getOrder().getId());
        dto.setReason(returnRequest.getReason());
        dto.setStatus(returnRequest.getStatus().getCode());
        return dto;
    }

    public static RefundResponseDTO toResponse(Refund refund) {
        RefundResponseDTO dto = new RefundResponseDTO();
        dto.setId(refund.getId());
        dto.setReturnRequestId(refund.getReturnRequest().getId());
        dto.setAmount(refund.getAmount());
        dto.setStatus(refund.getStatus().getCode());
        dto.setProcessedAt(refund.getProcessedAt());
        return dto;
    }
}
