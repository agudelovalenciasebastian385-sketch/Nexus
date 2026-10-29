package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.InventoryMovementResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;

/**
 * Mapper from the Inventory Domain Model (with its movements) to its REST DTO.
 */
public final class InventoryRestMapper {

    private InventoryRestMapper() {
    }

    public static InventoryResponseDTO toResponse(Inventory inventory) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setProductId(inventory.getProduct().getId());
        dto.setProductName(inventory.getProduct().getName());
        dto.setWarehouseId(inventory.getWarehouse().getId());
        dto.setWarehouseName(inventory.getWarehouse().getName());
        dto.setQuantity(inventory.getQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setCondition(inventory.getCondition().getCode());
        dto.setMovements(inventory.getMovements().stream().map(InventoryRestMapper::toResponse).toList());
        return dto;
    }

    public static List<InventoryResponseDTO> toResponse(List<Inventory> inventories) {
        return inventories.stream().map(InventoryRestMapper::toResponse).toList();
    }

    private static InventoryMovementResponseDTO toResponse(InventoryMovement movement) {
        InventoryMovementResponseDTO dto = new InventoryMovementResponseDTO();
        dto.setType(movement.getType().getCode());
        dto.setQuantity(movement.getQuantity());
        dto.setDate(movement.getDate());
        return dto;
    }
}
