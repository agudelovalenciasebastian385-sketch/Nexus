package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;

/**
 * Input port: UpdateBuyerProfile use case.
 *
 * <p>Updates the profile of the requesting buyer: full name, primary address and additional addresses.</p>
 */
public interface UpdateBuyerProfileUseCase {

    Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress,
                             List<Address> additionalAddresses);
}
