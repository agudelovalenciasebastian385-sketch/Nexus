package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;

/**
 * Input port: DispatchShipment use case.
 *
 * <p>Dispatches a packed shipment ({@code PACKED} → {@code IN_TRANSIT}), converts its reservations into sale outbounds and moves the order to {@code SHIPPED}.</p>
 */
public interface DispatchShipmentUseCase {

    Shipment dispatchShipment(User requestingUser, String shipmentId);
}
