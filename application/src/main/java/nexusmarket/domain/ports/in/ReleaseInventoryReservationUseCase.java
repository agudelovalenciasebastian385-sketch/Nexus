package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;

/**
 * Input port: ReleaseInventoryReservation use case.
 *
 * <p>Returns reserved stock to the available stock ({@code RESERVATION_RELEASE} movement).</p>
 */
public interface ReleaseInventoryReservationUseCase {

    Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId,
                                          int quantity);
}
