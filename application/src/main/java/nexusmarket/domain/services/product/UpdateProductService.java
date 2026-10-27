package nexusmarket.domain.services.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.Variant;
import org.springframework.stereotype.Service;

/**
 * Updates the commercial information of a product of the requesting seller
 * (product-services.md - UpdateProduct).
 */
@Service
@RequiredArgsConstructor
public class UpdateProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public Product updateProduct(User requestingUser, String productId, String name, String description,
                                 BigDecimal price, List<Variant> variants) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.SELLER);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        validateSellerOwnershipService.execute(requester, product);

        product.updateInfo(name, description, price, variants);
        productRepositoryPort.update(product);
        registerAuditLogService.execute(requester, AuditAction.PRODUCT_UPDATED, AuditEntityType.PRODUCT,
                product.getId(), Map.of("price", price.toPlainString()));
        return product;
    }
}
