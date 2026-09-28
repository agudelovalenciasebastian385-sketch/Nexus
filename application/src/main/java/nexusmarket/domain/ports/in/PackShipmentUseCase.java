package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;

/**
 * Input port: PackShipment use case.
 *
 * <p>Registers that a shipment has been packed ({@code PREPARING} → {@code PACKED}).</p>
 */
public interface PackShipmentUseCase {

    Shipment packShipment(User requestingUser, String shipmentId);
}
