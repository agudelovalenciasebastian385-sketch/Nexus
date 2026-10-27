package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;

/**
 * Role input port (public, no authentication required).
 *
 * <p>Public operations that do not require a specific role: buyer self-registration, login, logout and the public catalog.</p>
 */
public interface PublicAccessPort {

    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword,
                            Address primaryAddress);

    AuthenticationResult login(String email, String rawPassword);

    void logout(User user);

    List<Product> consultCatalog();

    Product consultCatalogProduct(String productId);
}
