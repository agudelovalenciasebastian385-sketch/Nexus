package nexusmarket.domain.services.inventory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Applies a manual correction of stock and/or changes the condition of an
 * inventory record, e.g. marks it {@code DAMAGED} (inventory-services.md -
 * RegisterInventoryAdjustment). Restricted to logistics operators.
 */
@Service
@RequiredArgsConstructor
public class RegisterInventoryAdjustmentService {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId,
                                                 int quantity, InventoryCondition condition) {
        User operator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        if (quantity == 0 && condition == null) {
            throw new InvalidInventoryException("An adjustment requires a non-zero quantity or a new condition.");
        }
        PhysicalProduct product = findPhysicalProduct(productId);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", productId + ":" + warehouseId));

        Map<String, String> details = new HashMap<>();
        if (quantity != 0) {
            inventory.registerMovement(MovementType.ADJUSTMENT, quantity, LocalDateTime.now());
            details.put("quantity", String.valueOf(quantity));
        }
        if (condition != null) {
            details.put("condition", inventory.getCondition().getCode() + "->" + condition.getCode());
            inventory.setCondition(condition);
        }
        inventoryRepositoryPort.update(inventory);
        registerAuditLogService.execute(operator, AuditAction.INVENTORY_ADJUSTED, AuditEntityType.INVENTORY,
                productId + ":" + warehouseId, details);
        return inventory;
    }

    private PhysicalProduct findPhysicalProduct(String productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!(product instanceof PhysicalProduct physicalProduct)) {
            throw new InvalidInventoryException("Inventory only applies to physical products.");
        }
        return physicalProduct;
    }
}
