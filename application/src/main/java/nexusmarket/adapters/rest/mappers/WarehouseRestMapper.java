package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import nexusmarket.domain.models.Warehouse;

/**
 * Mapper from the Warehouse Domain Model to its REST DTO.
 */
public final class WarehouseRestMapper {

    private WarehouseRestMapper() {
    }

    public static WarehouseResponseDTO toResponse(Warehouse warehouse) {
        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setType(warehouse.getType().getCode());
        dto.setLocation(AddressRestMapper.toResponse(warehouse.getLocation()));
        dto.setSellerId(warehouse.getSeller() != null ? warehouse.getSeller().getId() : null);
        return dto;
    }

    public static List<WarehouseResponseDTO> toResponse(List<Warehouse> warehouses) {
        return warehouses.stream().map(WarehouseRestMapper::toResponse).toList();
    }
}
