package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import nexusmarket.domain.models.Invoice;

/**
 * Mapper from the Invoice Domain Model to its REST DTO.
 */
public final class InvoiceRestMapper {

    private InvoiceRestMapper() {
    }

    public static InvoiceResponseDTO toResponse(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setOrderId(invoice.getOrder().getId());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setIssuedAt(invoice.getIssuedAt());
        return dto;
    }
}
