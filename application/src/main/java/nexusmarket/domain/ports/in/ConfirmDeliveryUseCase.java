package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;

/**
 * Input port: ConfirmDelivery use case.
 *
 * <p>Confirms the delivery of a shipment ({@code IN_TRANSIT} → {@code DELIVERED}) and finalizes the order when all its shipments are delivered.</p>
 */
public interface ConfirmDeliveryUseCase {

    Shipment confirmDelivery(User requestingUser, String shipmentId);
}
