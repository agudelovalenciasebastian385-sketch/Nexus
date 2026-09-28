package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;

/**
 * Input port: CreateShipments use case.
 *
 * <p>Creates the shipments of a {@code PAID} order: one per origin warehouse, only for physical items.</p>
 */
public interface CreateShipmentsUseCase {

    List<Shipment> createShipments(User requestingUser, String orderId);
}
