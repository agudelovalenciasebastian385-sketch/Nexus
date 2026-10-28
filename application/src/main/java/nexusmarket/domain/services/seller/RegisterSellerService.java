package nexusmarket.domain.services.seller;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.stereotype.Service;

/**
 * Onboards a seller together with its first {@code SELLER} warehouse
 * (seller-services.md - RegisterSeller). Sellers cannot self-register: only an
 * administrator can execute this operation.
 */
@Service
@RequiredArgsConstructor
public class RegisterSellerService {

    private final UserRepositoryPort userRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email,
                                 String rawPassword, String warehouseName, Address warehouseLocation) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        validateUniqueness(identificationNumber, email);
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidUserException("Password cannot be empty.");
        }

        Seller seller = new Seller(UUID.randomUUID().toString(), identificationNumber, fullName, email,
                passwordServicePort.encrypt(rawPassword), UserStatus.ACTIVE);
        Warehouse warehouse = new Warehouse(UUID.randomUUID().toString(), warehouseName, warehouseLocation,
                WarehouseType.SELLER, seller);
        seller.addWarehouse(warehouse);

        Seller saved = sellerRepositoryPort.save(seller);
        warehouseRepositoryPort.save(warehouse);
        registerAuditLogService.execute(administrator, AuditAction.SELLER_REGISTERED, AuditEntityType.SELLER,
                saved.getId(), Map.of("email", saved.getEmail(), "firstWarehouse", warehouse.getId()));
        registerAuditLogService.execute(administrator, AuditAction.WAREHOUSE_REGISTERED, AuditEntityType.WAREHOUSE,
                warehouse.getId(), Map.of("type", WarehouseType.SELLER.getCode(), "seller", saved.getId()));
        return saved;
    }

    private void validateUniqueness(String identificationNumber, String email) {
        if (identificationNumber != null && userRepositoryPort.existsByIdentificationNumber(identificationNumber)) {
            throw new UserAlreadyExistsException(
                    "A user with identification number " + identificationNumber + " is already registered.");
        }
        String normalizedEmail = User.normalizeEmail(email);
        if (normalizedEmail != null && userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("A user with email " + normalizedEmail + " is already registered.");
        }
    }
}
