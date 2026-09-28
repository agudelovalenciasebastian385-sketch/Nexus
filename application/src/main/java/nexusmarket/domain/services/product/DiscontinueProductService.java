package nexusmarket.domain.services.product;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.DiscontinueProductUseCase;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Permanently withdraws a product from the catalog (→ {@code DISCONTINUED}) (product-services.md - DiscontinueProduct).
 * The transition is enforced by {@code Product.discontinue()}.
 */
@Service
@RequiredArgsConstructor
public class DiscontinueProductService implements DiscontinueProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Product discontinueProduct(User requestingUser, String productId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.SELLER);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        validateSellerOwnershipService.execute(requester, product);

        ProductStatus previousStatus = product.getStatus();
        product.discontinue();
        productRepositoryPort.update(product);
        registerAuditLogService.execute(requester, AuditAction.PRODUCT_DISCONTINUED, AuditEntityType.PRODUCT, product.getId(),
                Map.of("from", previousStatus.getCode(), "to", product.getStatus().getCode()));
        return product;
    }
}
