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
 * Returns reserved stock to the available stock with a
 * {@code RESERVATION_RELEASE} movement (inventory-services.md -
 * ReleaseInventoryReservation). Restricted to logistics operators.
 */
@Service
@RequiredArgsConstructor
public class ReleaseInventoryReservationService {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId,
                                                 int quantity) {
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

        inventory.registerMovement(MovementType.RESERVATION_RELEASE, quantity, LocalDateTime.now());
        inventoryRepositoryPort.update(inventory);
        registerAuditLogService.execute(operator, AuditAction.INVENTORY_RESERVATION_RELEASED,
                AuditEntityType.INVENTORY, productId + ":" + warehouseId, Map.of("quantity", String.valueOf(quantity)));
        return inventory;
    }
}
