package nexusmarket.domain.services.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.RegisterProductUseCase;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.Variant;
import org.springframework.stereotype.Service;

/**
 * Registers a physical or digital product of the requesting seller in
 * {@code DRAFT} status (product-services.md - RegisterProduct).
 */
@Service
@RequiredArgsConstructor
public class RegisterProductService implements RegisterProductUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Product registerProduct(User requestingUser, ProductType type, String name, String description,
                                   BigDecimal price, List<Variant> variants) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.SELLER);
        if (type == null) {
            throw new InvalidProductException("Product type is required.");
        }
        Seller seller = sellerRepositoryPort.findById(requester.getId())
                .orElseThrow(() -> new EntityNotFoundException("Seller", requester.getId()));

        String id = UUID.randomUUID().toString();
        Product product = type == ProductType.PHYSICAL
                ? new PhysicalProduct(id, name, description, price, seller, variants, ProductStatus.DRAFT)
                : new DigitalProduct(id, name, description, price, seller, variants, ProductStatus.DRAFT);
        seller.addProduct(product);

        Product saved = productRepositoryPort.save(product);
        registerAuditLogService.execute(requester, AuditAction.PRODUCT_REGISTERED, AuditEntityType.PRODUCT,
                saved.getId(), Map.of("type", type.getCode(), "price", price.toPlainString()));
        return saved;
    }
}
