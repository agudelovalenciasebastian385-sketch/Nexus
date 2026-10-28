package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import nexusmarket.domain.models.Buyer;

/**
 * Mapper from the Buyer Domain Model to its REST DTO. The password hash is never copied.
 */
public final class BuyerRestMapper {

    private BuyerRestMapper() {
    }

    public static BuyerResponseDTO toResponse(Buyer buyer) {
        BuyerResponseDTO dto = new BuyerResponseDTO();
        dto.setId(buyer.getId());
        dto.setIdentificationNumber(buyer.getIdentificationNumber());
        dto.setFullName(buyer.getFullName());
        dto.setEmail(buyer.getEmail());
        dto.setStatus(buyer.getStatus().getCode());
        dto.setCommercialStatus(buyer.getCommercialStatus().getCode());
        dto.setPrimaryAddress(AddressRestMapper.toResponse(buyer.getPrimaryAddress()));
        dto.setAdditionalAddresses(AddressRestMapper.toResponse(buyer.getAdditionalAddresses()));
        return dto;
    }
}
