package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultSeller use case.
 *
 * <p>Retrieves the information of a seller.</p>
 */
public interface ConsultSellerUseCase {

    Seller consultSeller(User requestingUser, String sellerId);
}
