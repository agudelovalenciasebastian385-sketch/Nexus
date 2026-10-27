package nexusmarket.domain.services.warehouse;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.stereotype.Service;

/**
 * Registers a warehouse (warehouse-services.md - RegisterWarehouse): the
 * administrator registers {@code MARKETPLACE} warehouses and a seller registers
 * additional {@code SELLER} warehouses of its own.
 */
@Service
@RequiredArgsConstructor
public class RegisterWarehouseService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.ADMINISTRATOR, SystemRole.SELLER);
        if (type == null) {
            throw new InvalidWarehouseException("Warehouse type is required.");
        }
        Warehouse warehouse;
        if (requester.getRole() == SystemRole.ADMINISTRATOR) {
            if (type != WarehouseType.MARKETPLACE) {
                throw new UnauthorizedOperationException(
                        "An administrator can only register " + WarehouseType.MARKETPLACE.getCode() + " warehouses.");
            }
            warehouse = new Warehouse(UUID.randomUUID().toString(), name, location, type, null);
        } else {
            if (type != WarehouseType.SELLER) {
                throw new UnauthorizedOperationException(
                        "A seller can only register " + WarehouseType.SELLER.getCode() + " warehouses.");
            }
            Seller seller = sellerRepositoryPort.findById(requester.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Seller", requester.getId()));
            warehouse = new Warehouse(UUID.randomUUID().toString(), name, location, type, seller);
            seller.addWarehouse(warehouse);
        }

        Warehouse saved = warehouseRepositoryPort.save(warehouse);
        registerAuditLogService.execute(requester, AuditAction.WAREHOUSE_REGISTERED, AuditEntityType.WAREHOUSE,
                saved.getId(), Map.of("type", type.getCode()));
        return saved;
    }
}
