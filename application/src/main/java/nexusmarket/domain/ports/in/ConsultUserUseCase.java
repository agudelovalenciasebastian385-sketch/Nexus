package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;

/**
 * Input port: ConsultUser use case.
 *
 * <p>Retrieves the information of a user.</p>
 */
public interface ConsultUserUseCase {

    User consultUser(User requestingUser, String userId);
}
