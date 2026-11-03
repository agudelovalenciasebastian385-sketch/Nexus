package nexusmarket.adapters.persistence.jpa.mappers;

import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaId;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;
import org.junit.jupiter.api.Test;

class InventoryJpaMapperTest {

    private final Seller seller = DomainFixtures.seller();
    private final PhysicalProduct product = DomainFixtures.publishedPhysical(seller);
    private final Warehouse warehouse = DomainFixtures.sellerWarehouse(seller);
    private final Inventory inventory = new Inventory(product, warehouse, 8, 2, InventoryCondition.DAMAGED, null);

    private Inventory roundTrip(List<InventoryMovement> movements) {
        return InventoryJpaMapper.toDomain(InventoryJpaMapper.toEntity(inventory), product, warehouse, movements);
    }

    @Test
    void compositeKeyHoldsTheProductAndTheWarehouse() {
        assertThat(InventoryJpaMapper.toEntity(inventory).getId())
                .isEqualTo(new InventoryJpaId(product.getId(), warehouse.getId()));
    }

    @Test
    void quantitiesSurviveTheRoundTrip() {
        Inventory restored = roundTrip(List.of());

        assertThat(restored.getQuantity()).isEqualTo(8);
        assertThat(restored.getReservedQuantity()).isEqualTo(2);
    }

    @Test
    void conditionSurvivesTheRoundTrip() {
        assertThat(roundTrip(List.of()).getCondition()).isEqualTo(InventoryCondition.DAMAGED);
    }

    @Test
    void suppliedMovementsAreRestored() {
        InventoryMovement movement = new InventoryMovement(MovementType.INBOUND, 8, NOW);

        assertThat(roundTrip(List.of(movement)).getMovements()).containsExactly(movement);
    }

    @Test
    void copyToEntityCopiesQuantitiesAndCondition() {
        InventoryJpaEntity managed = new InventoryJpaEntity();

        InventoryJpaMapper.copyToEntity(inventory, managed);

        assertThat(managed.getQuantity()).isEqualTo(8);
        assertThat(managed.getReservedQuantity()).isEqualTo(2);
        assertThat(managed.getCondition()).isEqualTo("DAMAGED");
    }

    @Test
    void copyToEntityKeepsTheVersionReadAtTheStartOfTheUseCase() {
        InventoryJpaEntity managed = new InventoryJpaEntity();
        managed.setVersion(7L);

        InventoryJpaMapper.copyToEntity(inventory, managed);

        assertThat(managed.getVersion()).isEqualTo(7L);
    }

    @Test
    void copyToEntityKeepsTheCompositeKey() {
        InventoryJpaEntity managed = new InventoryJpaEntity();
        InventoryJpaId id = new InventoryJpaId("stored-product", "stored-warehouse");
        managed.setId(id);

        InventoryJpaMapper.copyToEntity(inventory, managed);

        assertThat(managed.getId()).isSameAs(id);
    }

    @Test
    void movementSurvivesTheRoundTrip() {
        InventoryMovement movement = new InventoryMovement(MovementType.ADJUSTMENT, -3, NOW);

        InventoryMovement restored = InventoryMovementJpaMapper.toDomain(
                InventoryMovementJpaMapper.toEntity(movement, product.getId(), warehouse.getId()));

        assertThat(restored).usingRecursiveComparison().isEqualTo(movement);
    }

    @Test
    void movementReferencesItsInventory() {
        InventoryMovement movement = new InventoryMovement(MovementType.INBOUND, 1, NOW);

        assertThat(InventoryMovementJpaMapper.toEntity(movement, "p-1", "w-1").getWarehouseId()).isEqualTo("w-1");
    }
}
