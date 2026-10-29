package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.valueobjects.MovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InventoryMovementTest {

    @ParameterizedTest
    @EnumSource(value = MovementType.class, mode = EnumSource.Mode.EXCLUDE, names = "ADJUSTMENT")
    void zeroQuantityIsRejectedForEveryMovementButAdjustment(MovementType type) {
        assertThatThrownBy(() -> new InventoryMovement(type, 0, NOW)).isInstanceOf(InvalidInventoryException.class);
    }

    @ParameterizedTest
    @EnumSource(value = MovementType.class, mode = EnumSource.Mode.EXCLUDE, names = "ADJUSTMENT")
    void negativeQuantityIsRejectedForEveryMovementButAdjustment(MovementType type) {
        assertThatThrownBy(() -> new InventoryMovement(type, -1, NOW)).isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void zeroAdjustmentIsRejected() {
        assertThatThrownBy(() -> new InventoryMovement(MovementType.ADJUSTMENT, 0, NOW))
                .isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void negativeAdjustmentIsAccepted() {
        assertThat(new InventoryMovement(MovementType.ADJUSTMENT, -3, NOW).getQuantity()).isEqualTo(-3);
    }

    @Test
    void typeIsRequired() {
        assertThatThrownBy(() -> new InventoryMovement(null, 1, NOW)).isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void dateIsRequired() {
        assertThatThrownBy(() -> new InventoryMovement(MovementType.INBOUND, 1, null))
                .isInstanceOf(InvalidInventoryException.class);
    }
}
