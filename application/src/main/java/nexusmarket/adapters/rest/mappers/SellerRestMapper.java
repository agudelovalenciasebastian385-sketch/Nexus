package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import nexusmarket.domain.models.Seller;

/**
 * Mapper from the Seller Domain Model to its REST DTO. The password hash is never copied.
 */
public final class SellerRestMapper {

    private SellerRestMapper() {
    }

    public static SellerResponseDTO toResponse(Seller seller) {
        SellerResponseDTO dto = new SellerResponseDTO();
        dto.setId(seller.getId());
        dto.setIdentificationNumber(seller.getIdentificationNumber());
        dto.setFullName(seller.getFullName());
        dto.setEmail(seller.getEmail());
        dto.setStatus(seller.getStatus().getCode());
        return dto;
    }
}
