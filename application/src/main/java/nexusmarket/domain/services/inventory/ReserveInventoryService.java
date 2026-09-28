package nexusmarket.domain.services.inventory;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.ReserveInventoryUseCase;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Holds stock of a physical product for an order in progress: a
 * {@code RESERVATION} movement moves units from available to reserved
 * (inventory-services.md - ReserveInventory). Inventory that does not exist or
 * is {@code DAMAGED} can never be reserved.
 */
@Service
@RequiredArgsConstructor
public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity) {
        User operator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!(product instanceof PhysicalProduct physicalProduct)) {
            throw new InvalidInventoryException("Inventory only applies to physical products.");
        }
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(physicalProduct, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", productId + ":" + warehouseId));
        return execute(inventory, quantity, operator);
    }

    /**
     * Internal reservation used by {@code ConfirmOrderService}: applies the movement, stores the record and
     * audits it. The caller is responsible for the authorization of {@code performedBy}.
     */
    public Inventory execute(Inventory inventory, int quantity, User performedBy) {
        inventory.registerMovement(MovementType.RESERVATION, quantity, LocalDateTime.now());
        inventoryRepositoryPort.update(inventory);
        registerAuditLogService.execute(performedBy, AuditAction.INVENTORY_RESERVED, AuditEntityType.INVENTORY,
                inventory.getProduct().getId() + ":" + inventory.getWarehouse().getId(),
                Map.of("quantity", String.valueOf(quantity)));
        return inventory;
    }
}
