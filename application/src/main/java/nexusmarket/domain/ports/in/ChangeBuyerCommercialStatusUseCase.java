package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.CommercialStatus;

/**
 * Input port: ChangeBuyerCommercialStatus use case.
 *
 * <p>Changes the commercial status of a buyer ({@code ENABLED}, {@code RESTRICTED}, {@code SUSPENDED}).</p>
 */
public interface ChangeBuyerCommercialStatusUseCase {

    Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus);
}
