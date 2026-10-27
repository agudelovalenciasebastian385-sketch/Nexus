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
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Adds stock to an existing inventory record with an {@code INBOUND} movement
 * (inventory-services.md - RegisterInventoryInbound).
 */
@Service
@RequiredArgsConstructor
public class RegisterInventoryInboundService {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                              int quantity) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR);
        PhysicalProduct product = findPhysicalProduct(productId);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        validateSellerOwnershipService.execute(requester, product);
        validateSellerOwnershipService.execute(requester, warehouse);
        Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", productId + ":" + warehouseId));

        inventory.registerMovement(MovementType.INBOUND, quantity, LocalDateTime.now());
        inventoryRepositoryPort.update(inventory);
        registerAuditLogService.execute(requester, AuditAction.INVENTORY_INBOUND_REGISTERED, AuditEntityType.INVENTORY,
                productId + ":" + warehouseId, Map.of("quantity", String.valueOf(quantity)));
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
