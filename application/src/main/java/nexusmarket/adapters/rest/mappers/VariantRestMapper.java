package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.VariantRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.VariantResponseDTO;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Mapper between the Variant value object and its REST DTOs. The domain validates the values.
 */
public final class VariantRestMapper {

    private VariantRestMapper() {
    }

    public static Variant toDomain(VariantRequestDTO dto) {
        return dto == null ? null : new Variant(dto.getType(), dto.getValue());
    }

    public static List<Variant> toDomain(List<VariantRequestDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(VariantRestMapper::toDomain).toList();
    }

    /**
     * Variant sent as query parameters (DELETE of a cart line): no variant when both are absent.
     * When only one is present the Variant constructor rejects it (InvalidVariantException, 400).
     */
    public static Variant fromQuery(String type, String value) {
        if (type == null && value == null) {
            return null;
        }
        return new Variant(type, value);
    }

    public static VariantResponseDTO toResponse(Variant variant) {
        if (variant == null) {
            return null;
        }
        VariantResponseDTO dto = new VariantResponseDTO();
        dto.setType(variant.getType());
        dto.setValue(variant.getValue());
        return dto;
    }
}
