package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;

/**
 * Input port: ApproveReturn use case.
 *
 * <p>Approves a return request, re-enters the inventory of its physical items and creates the pending refund.</p>
 */
public interface ApproveReturnUseCase {

    ReturnRequest approveReturn(User requestingUser, String returnRequestId);
}
