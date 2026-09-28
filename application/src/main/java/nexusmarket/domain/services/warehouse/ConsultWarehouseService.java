package nexusmarket.domain.services.warehouse;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.ConsultWarehouseUseCase;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves one warehouse or the warehouses visible to the requesting user
 * (warehouse-services.md - ConsultWarehouse). A seller only sees its own.
 */
@Service
@RequiredArgsConstructor
public class ConsultWarehouseService implements ConsultWarehouseUseCase {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Override
    public Warehouse consultWarehouse(User requestingUser, String warehouseId) {
        User requester = authorize(requestingUser);
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        validateSellerOwnershipService.execute(requester, warehouse);
        return warehouse;
    }

    @Override
    public List<Warehouse> consultWarehouses(User requestingUser) {
        User requester = authorize(requestingUser);
        if (requester.getRole() == SystemRole.SELLER) {
            Seller seller = sellerRepositoryPort.findById(requester.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Seller", requester.getId()));
            return warehouseRepositoryPort.findBySeller(seller);
        }
        return warehouseRepositoryPort.findAll();
    }

    private User authorize(User requestingUser) {
        return validateRoleAuthorizationService.execute(requestingUser, SystemRole.SELLER,
                SystemRole.LOGISTICS_OPERATOR, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
    }
}
