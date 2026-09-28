package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultSellerProducts use case.
 *
 * <p>Retrieves all the products of a seller, in any status.</p>
 */
public interface ConsultSellerProductsUseCase {

    List<Product> consultSellerProducts(User requestingUser, String sellerId);
}
