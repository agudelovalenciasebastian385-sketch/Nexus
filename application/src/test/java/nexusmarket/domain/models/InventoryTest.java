package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InventoryTest {

    private final Seller seller = seller();
    private final PhysicalProduct product = publishedPhysical(seller);
    private final Warehouse warehouse = sellerWarehouse(seller);

    private Inventory inventory(int quantity, int reserved, InventoryCondition condition) {
        return new Inventory(product, warehouse, quantity, reserved, condition, null);
    }

    private Inventory available(int quantity) {
        return inventory(quantity, 0, InventoryCondition.AVAILABLE);
    }

    // ------------------------------------------------------------------ movements

    @Test
    void inboundAddsToAvailableStock() {
        Inventory inventory = available(10);

        inventory.registerMovement(MovementType.INBOUND, 5, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    void reservationMovesStockFromAvailableToReserved() {
        Inventory inventory = available(10);

        inventory.registerMovement(MovementType.RESERVATION, 3, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(7);
        assertThat(inventory.getReservedQuantity()).isEqualTo(3);
    }

    @Test
    void releasingAReservationReturnsStockToAvailable() {
        Inventory inventory = inventory(7, 3, InventoryCondition.AVAILABLE);

        inventory.registerMovement(MovementType.RESERVATION_RELEASE, 2, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(9);
        assertThat(inventory.getReservedQuantity()).isEqualTo(1);
    }

    @Test
    void saleOutboundConsumesReservedStockOnly() {
        Inventory inventory = inventory(7, 3, InventoryCondition.AVAILABLE);

        inventory.registerMovement(MovementType.SALE_OUTBOUND, 3, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(7);
        assertThat(inventory.getReservedQuantity()).isZero();
    }

    @Test
    void positiveAdjustmentIncreasesAvailableStock() {
        Inventory inventory = available(10);

        inventory.registerMovement(MovementType.ADJUSTMENT, 4, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(14);
    }

    @Test
    void negativeAdjustmentDecreasesAvailableStock() {
        Inventory inventory = available(10);

        inventory.registerMovement(MovementType.ADJUSTMENT, -4, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(6);
    }

    @Test
    void returnAddsToAvailableStock() {
        Inventory inventory = available(8);

        inventory.registerMovement(MovementType.RETURN, 2, NOW);

        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    @Test
    void everyAcceptedMovementIsRecordedInTheHistory() {
        Inventory inventory = available(10);

        inventory.registerMovement(MovementType.RESERVATION, 2, NOW);
        inventory.registerMovement(MovementType.SALE_OUTBOUND, 2, NOW);

        assertThat(inventory.getMovements()).extracting(InventoryMovement::getType)
                .containsExactly(MovementType.RESERVATION, MovementType.SALE_OUTBOUND);
    }

    // ------------------------------------------------------------------ never negative

    @Test
    void reservingMoreThanAvailableStockIsRejected() {
        Inventory inventory = available(2);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.RESERVATION, 3, NOW))
                .isInstanceOf(InsufficientInventoryException.class);
    }

    @Test
    void releasingMoreThanReservedStockIsRejected() {
        Inventory inventory = inventory(5, 1, InventoryCondition.AVAILABLE);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.RESERVATION_RELEASE, 2, NOW))
                .isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void sellingMoreThanReservedStockIsRejected() {
        Inventory inventory = inventory(5, 1, InventoryCondition.AVAILABLE);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.SALE_OUTBOUND, 2, NOW))
                .isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void adjustmentThatWouldMakeStockNegativeIsRejected() {
        Inventory inventory = available(3);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.ADJUSTMENT, -4, NOW))
                .isInstanceOf(InsufficientInventoryException.class);
    }

    @Test
    void rejectedMovementLeavesStockAndHistoryUnchanged() {
        Inventory inventory = available(2);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.RESERVATION, 3, NOW))
                .isInstanceOf(InsufficientInventoryException.class);

        assertThat(inventory.getQuantity()).isEqualTo(2);
        assertThat(inventory.getReservedQuantity()).isZero();
        assertThat(inventory.getMovements()).isEmpty();
    }

    @Test
    void negativeInitialQuantitiesAreRejected() {
        assertThatThrownBy(() -> inventory(-1, 0, InventoryCondition.AVAILABLE))
                .isInstanceOf(InvalidInventoryException.class);
    }

    // ------------------------------------------------------------------ reservable condition

    @ParameterizedTest
    @EnumSource(value = InventoryCondition.class, names = {"RESERVED", "OUT_OF_STOCK", "DAMAGED"})
    void reservingInventoryThatIsNotAvailableIsRejected(InventoryCondition condition) {
        Inventory inventory = inventory(10, 0, condition);

        assertThatThrownBy(() -> inventory.registerMovement(MovementType.RESERVATION, 1, NOW))
                .isInstanceOf(InvalidInventoryException.class)
                .hasMessageContaining(condition.getCode());
    }

    @ParameterizedTest
    @EnumSource(value = InventoryCondition.class, names = {"RESERVED", "OUT_OF_STOCK", "DAMAGED"})
    void inventoryThatIsNotAvailableCannotBeReserved(InventoryCondition condition) {
        Inventory inventory = inventory(10, 0, condition);

        assertThat(inventory.canReserve(1)).isFalse();
    }

    @Test
    void availableInventoryWithEnoughStockCanBeReserved() {
        assertThat(available(5).canReserve(5)).isTrue();
    }

    @Test
    void availableInventoryWithoutEnoughStockCannotBeReserved() {
        assertThat(available(5).canReserve(6)).isFalse();
    }

    // ------------------------------------------------------------------ construction and identity

    @Test
    void missingConditionDefaultsToAvailable() {
        assertThat(inventory(1, 0, null).getCondition()).isEqualTo(InventoryCondition.AVAILABLE);
    }

    @Test
    void productIsRequired() {
        assertThatThrownBy(() -> new Inventory(null, warehouse, 1))
                .isInstanceOf(InvalidInventoryException.class);
    }

    @Test
    void inventoriesOfTheSameProductAndWarehouseAreEqual() {
        assertThat(available(1)).isEqualTo(available(9));
    }
}
