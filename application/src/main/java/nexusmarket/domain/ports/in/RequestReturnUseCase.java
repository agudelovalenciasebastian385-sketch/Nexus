package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;

/**
 * Input port: RequestReturn use case.
 *
 * <p>Registers a return request over a delivered order of the requesting buyer.</p>
 */
public interface RequestReturnUseCase {

    ReturnRequest requestReturn(User requestingUser, String orderId, String reason);
}
