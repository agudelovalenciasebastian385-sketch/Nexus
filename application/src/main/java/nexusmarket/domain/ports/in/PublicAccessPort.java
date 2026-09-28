package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.valueobjects.Address;

/**
 * Role input port (public, no authentication required).
 *
 * <p>Public operations that do not require an authenticated user: buyer self-registration, login and the public catalog.</p>
 */
public interface PublicAccessPort {

    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword,
                            Address primaryAddress);

    String login(String email, String rawPassword);

    List<Product> consultCatalog();

    Product consultCatalogProduct(String productId);
}
