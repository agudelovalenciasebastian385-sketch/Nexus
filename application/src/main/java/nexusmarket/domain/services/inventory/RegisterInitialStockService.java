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
 * Creates the inventory record of a physical product in a warehouse with its
 * initial stock, recorded as an {@code INBOUND} movement
 * (inventory-services.md - RegisterInitialStock).
 */
@Service
@RequiredArgsConstructor
public class RegisterInitialStockService {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR);
        PhysicalProduct product = findPhysicalProduct(productId);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        validateSellerOwnershipService.execute(requester, product);
        validateSellerOwnershipService.execute(requester, warehouse);
        if (inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse).isPresent()) {
            throw new InvalidInventoryException("Product " + productId + " already has an inventory record in warehouse "
                    + warehouseId + ".");
        }

        Inventory inventory = new Inventory(product, warehouse, 0);
        inventory.registerMovement(MovementType.INBOUND, quantity, LocalDateTime.now());
        product.addInventory(inventory);
        warehouse.addInventory(inventory);

        Inventory saved = inventoryRepositoryPort.save(inventory);
        registerAuditLogService.execute(requester, AuditAction.INVENTORY_INITIAL_STOCK_REGISTERED,
                AuditEntityType.INVENTORY, productId + ":" + warehouseId, Map.of("quantity", String.valueOf(quantity)));
        return saved;
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
