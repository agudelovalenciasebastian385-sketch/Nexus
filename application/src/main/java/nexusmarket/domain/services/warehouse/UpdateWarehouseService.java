package nexusmarket.domain.services.warehouse;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Updates the name and location of a warehouse (warehouse-services.md -
 * UpdateWarehouse). Type and owner never change.
 */
@Service
@RequiredArgsConstructor
public class UpdateWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.ADMINISTRATOR, SystemRole.SELLER);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        if (requester.getRole() == SystemRole.ADMINISTRATOR && !warehouse.isMarketplace()) {
            throw new UnauthorizedOperationException("An administrator can only update Marketplace warehouses.");
        }
        validateSellerOwnershipService.execute(requester, warehouse);

        warehouse.setName(name);
        warehouse.setLocation(location);
        warehouseRepositoryPort.update(warehouse);
        registerAuditLogService.execute(requester, AuditAction.WAREHOUSE_UPDATED, AuditEntityType.WAREHOUSE,
                warehouse.getId(), Map.of("name", name));
        return warehouse;
    }
}
