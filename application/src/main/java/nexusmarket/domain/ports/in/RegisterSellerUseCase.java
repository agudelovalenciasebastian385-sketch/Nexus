package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;

/**
 * Input port: RegisterSeller use case.
 *
 * <p>Onboards a seller together with its first warehouse of type {@code SELLER} in the same operation.</p>
 */
public interface RegisterSellerUseCase {

    Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email,
                          String rawPassword, String warehouseName, Address warehouseLocation);
}
