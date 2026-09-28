package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;

/**
 * Input port: RegisterInventoryInbound use case.
 *
 * <p>Adds stock to an existing inventory record ({@code INBOUND} movement).</p>
 */
public interface RegisterInventoryInboundUseCase {

    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                       int quantity);
}
