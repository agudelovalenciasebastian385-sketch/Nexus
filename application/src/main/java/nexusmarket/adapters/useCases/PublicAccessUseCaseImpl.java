package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.PublicAccessPort;
import nexusmarket.domain.services.product.ConsultCatalogService;
import nexusmarket.domain.services.user.LoginService;
import nexusmarket.domain.services.user.LogoutService;
import nexusmarket.domain.services.user.RegisterBuyerUserService;
import nexusmarket.domain.valueobjects.Address;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link PublicAccessPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final RegisterBuyerUserService registerBuyerUserService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ConsultCatalogService consultCatalogService;

    @Override
    public Buyer registerBuyerUser(String identificationNumber, String fullName, String email,
                                   String rawPassword, Address primaryAddress) {
        return registerBuyerUserService.registerBuyerUser(identificationNumber, fullName, email, rawPassword, primaryAddress);
    }

    @Override
    public AuthenticationResult login(String email, String rawPassword) {
        return loginService.login(email, rawPassword);
    }

    @Override
    public void logout(User user) {
        logoutService.logout(user);
    }

    @Override
    public List<Product> consultCatalog() {
        return consultCatalogService.consultCatalog();
    }

    @Override
    public Product consultCatalogProduct(String productId) {
        return consultCatalogService.consultCatalogProduct(productId);
    }
}
