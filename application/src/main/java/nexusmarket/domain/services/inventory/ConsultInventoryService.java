package nexusmarket.domain.services.inventory;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.ConsultInventoryUseCase;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the inventory records of a product or of a warehouse
 * (inventory-services.md - ConsultInventory). A seller only consults its own
 * products and warehouses.
 */
@Service
@RequiredArgsConstructor
public class ConsultInventoryService implements ConsultInventoryUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Override
    public List<Inventory> consultProductInventory(User requestingUser, String productId) {
        User requester = authorize(requestingUser);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!(product instanceof PhysicalProduct physicalProduct)) {
            throw new InvalidInventoryException("Inventory only applies to physical products.");
        }
        validateSellerOwnershipService.execute(requester, product);
        return inventoryRepositoryPort.findByProduct(physicalProduct);
    }

    @Override
    public List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId) {
        User requester = authorize(requestingUser);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        validateSellerOwnershipService.execute(requester, warehouse);
        return inventoryRepositoryPort.findByWarehouse(warehouse);
    }

    private User authorize(User requestingUser) {
        return validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR, SystemRole.SUPERVISOR);
    }
}
