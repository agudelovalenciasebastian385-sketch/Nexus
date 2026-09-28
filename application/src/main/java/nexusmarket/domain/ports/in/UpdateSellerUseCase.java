package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;

/**
 * Input port: UpdateSeller use case.
 *
 * <p>Updates the full name of a seller.</p>
 */
public interface UpdateSellerUseCase {

    Seller updateSeller(User requestingUser, String sellerId, String fullName);
}
