package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.valueobjects.Address;

/**
 * Input port: RegisterBuyerUser use case.
 *
 * <p>Registers a new buyer who self-registers in the Marketplace, together with its unique shopping cart.</p>
 */
public interface RegisterBuyerUserUseCase {

    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword,
                            Address primaryAddress);
}
