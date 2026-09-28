package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultBuyer use case.
 *
 * <p>Retrieves the information of a buyer.</p>
 */
public interface ConsultBuyerUseCase {

    Buyer consultBuyer(User requestingUser, String buyerId);
}
