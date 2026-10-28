package nexusmarket.domain.services.user;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Registers a buyer who self-registers in the Marketplace, together with its
 * unique cart (user-services.md - RegisterBuyerUser). Public operation.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final RegisterAuditLogService registerAuditLogService;

    public Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword,
                                   Address primaryAddress) {
        validateUniqueness(identificationNumber, email);
        String encodedPassword = encrypt(rawPassword);

        Buyer buyer = new Buyer(UUID.randomUUID().toString(), identificationNumber, fullName, email,
                encodedPassword, UserStatus.ACTIVE, primaryAddress, CommercialStatus.ENABLED);
        Cart cart = new Cart(UUID.randomUUID().toString(), buyer);
        buyer.assignCart(cart);

        Buyer saved = buyerRepositoryPort.save(buyer);
        cartRepositoryPort.save(cart);
        registerAuditLogService.execute(saved, AuditAction.USER_REGISTERED, AuditEntityType.BUYER, saved.getId(),
                Map.of("email", saved.getEmail(), "cart", cart.getId()));
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

    private String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidUserException("Password cannot be empty.");
        }
        return passwordServicePort.encrypt(rawPassword);
    }
}
