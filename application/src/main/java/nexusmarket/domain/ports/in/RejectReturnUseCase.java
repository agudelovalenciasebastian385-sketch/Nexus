package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;

/**
 * Input port: RejectReturn use case.
 *
 * <p>Rejects a return request that does not meet the required conditions.</p>
 */
public interface RejectReturnUseCase {

    ReturnRequest rejectReturn(User requestingUser, String returnRequestId);
}
