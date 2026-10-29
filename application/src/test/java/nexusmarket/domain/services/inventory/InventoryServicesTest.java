package nexusmarket.domain.services.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InventoryServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final LogisticsOperator operator = h.operator();

    @Nested
    class RegisterInitialStock {

        @Test
        void sellerRegistersStockOfItsProductInItsWarehouse() {
            Inventory inventory = h.registerInitialStock.registerInitialStock(seller, mouse.getId(), warehouse.getId(), 10);

            assertThat(inventory.getQuantity()).isEqualTo(10);
            assertThat(h.inventories.findByProductAndWarehouse(mouse, warehouse)).contains(inventory);
        }

        @Test
        void initialStockIsRecordedAsAnInboundMovement() {
            Inventory inventory = h.registerInitialStock.registerInitialStock(seller, mouse.getId(), warehouse.getId(), 10);

            assertThat(inventory.getMovements()).extracting(InventoryMovement::getType).containsExactly(MovementType.INBOUND);
        }

        @Test
        void initialStockIsAudited() {
            h.registerInitialStock.registerInitialStock(seller, mouse.getId(), warehouse.getId(), 10);

            assertThat(h.audit.last(AuditAction.INVENTORY_INITIAL_STOCK_REGISTERED).getEntityId())
                    .isEqualTo(mouse.getId() + ":" + warehouse.getId());
        }

        @Test
        void operatorRegistersStockInAMarketplaceWarehouse() {
            Warehouse hub = h.marketplaceWarehouse();

            Inventory inventory = h.registerInitialStock.registerInitialStock(operator, mouse.getId(), hub.getId(), 4);

            assertThat(inventory.getWarehouse()).isEqualTo(hub);
        }

        @Test
        void secondInitialStockForTheSamePairIsRejected() {
            h.registerInitialStock.registerInitialStock(seller, mouse.getId(), warehouse.getId(), 10);

            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, mouse.getId(),
                    warehouse.getId(), 5)).isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void digitalProductHasNoInventory() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, ebook.getId(),
                    warehouse.getId(), 5)).isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void sellerCannotUseAnotherSellersWarehouse() {
            Warehouse foreign = h.firstWarehouse(h.seller());

            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, mouse.getId(),
                    foreign.getId(), 5)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void sellerCannotStockAnotherSellersProduct() {
            PhysicalProduct foreign = h.physicalProduct(h.seller(), "10.00");

            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, foreign.getId(),
                    warehouse.getId(), 5)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void buyerIsNotAllowedToRegisterStock() {
            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(h.buyer(), mouse.getId(),
                    warehouse.getId(), 5)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void unknownWarehouseIsReported() {
            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, mouse.getId(), "missing", 5))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void unknownProductIsReported() {
            assertThatThrownBy(() -> h.registerInitialStock.registerInitialStock(seller, "missing", warehouse.getId(), 5))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class RegisterInbound {

        @Test
        void inboundAddsStockAndIsAudited() {
            h.stock(mouse, warehouse, 3);

            Inventory inventory = h.registerInventoryInbound.registerInventoryInbound(seller, mouse.getId(),
                    warehouse.getId(), 4);

            assertThat(inventory.getQuantity()).isEqualTo(7);
            assertThat(h.audit.actions()).contains(AuditAction.INVENTORY_INBOUND_REGISTERED);
        }

        @Test
        void inboundWithoutInventoryRecordIsReported() {
            assertThatThrownBy(() -> h.registerInventoryInbound.registerInventoryInbound(seller, mouse.getId(),
                    warehouse.getId(), 4)).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void sellerCannotRegisterAnInboundOfAnotherSellersProduct() {
            PhysicalProduct foreign = h.physicalProduct(h.seller(), "10.00");
            h.stock(foreign, warehouse, 1);

            assertThatThrownBy(() -> h.registerInventoryInbound.registerInventoryInbound(seller, foreign.getId(),
                    warehouse.getId(), 4)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void inboundOfADigitalProductIsRejected() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.registerInventoryInbound.registerInventoryInbound(seller, ebook.getId(),
                    warehouse.getId(), 4)).isInstanceOf(InvalidInventoryException.class);
        }
    }

    @Nested
    class RegisterAdjustment {

        @Test
        void negativeAdjustmentReducesTheStock() {
            h.stock(mouse, warehouse, 10);

            Inventory inventory = h.registerInventoryAdjustment.registerInventoryAdjustment(operator, mouse.getId(),
                    warehouse.getId(), -3, null);

            assertThat(inventory.getQuantity()).isEqualTo(7);
        }

        @Test
        void adjustmentChangesTheConditionAndAuditsIt() {
            h.stock(mouse, warehouse, 10);

            Inventory inventory = h.registerInventoryAdjustment.registerInventoryAdjustment(operator, mouse.getId(),
                    warehouse.getId(), 0, InventoryCondition.DAMAGED);

            assertThat(inventory.getCondition()).isEqualTo(InventoryCondition.DAMAGED);
            assertThat(h.audit.last(AuditAction.INVENTORY_ADJUSTED).getDetails())
                    .containsEntry("condition", "AVAILABLE->DAMAGED");
        }

        @Test
        void adjustmentWithoutChangesIsRejected() {
            h.stock(mouse, warehouse, 10);

            assertThatThrownBy(() -> h.registerInventoryAdjustment.registerInventoryAdjustment(operator, mouse.getId(),
                    warehouse.getId(), 0, null)).isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void adjustmentBelowZeroIsRejected() {
            h.stock(mouse, warehouse, 2);

            assertThatThrownBy(() -> h.registerInventoryAdjustment.registerInventoryAdjustment(operator, mouse.getId(),
                    warehouse.getId(), -3, null)).isInstanceOf(InsufficientInventoryException.class);
        }

        @Test
        void sellerCannotAdjustInventory() {
            h.stock(mouse, warehouse, 2);

            assertThatThrownBy(() -> h.registerInventoryAdjustment.registerInventoryAdjustment(seller, mouse.getId(),
                    warehouse.getId(), 1, null)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void adjustmentWithoutInventoryRecordIsReported() {
            assertThatThrownBy(() -> h.registerInventoryAdjustment.registerInventoryAdjustment(operator, mouse.getId(),
                    warehouse.getId(), 1, null)).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void adjustmentOfADigitalProductIsRejected() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.registerInventoryAdjustment.registerInventoryAdjustment(operator, ebook.getId(),
                    warehouse.getId(), 1, null)).isInstanceOf(InvalidInventoryException.class);
        }
    }

    @Nested
    class ReserveAndRelease {

        @Test
        void operatorReservesStock() {
            h.stock(mouse, warehouse, 10);

            Inventory inventory = h.reserveInventory.reserveInventory(operator, mouse.getId(), warehouse.getId(), 2);

            assertThat(inventory.getQuantity()).isEqualTo(8);
            assertThat(inventory.getReservedQuantity()).isEqualTo(2);
            assertThat(h.audit.actions()).contains(AuditAction.INVENTORY_RESERVED);
        }

        @Test
        void reservingDamagedInventoryIsRejected() {
            h.stock(mouse, warehouse, 10).setCondition(InventoryCondition.DAMAGED);

            assertThatThrownBy(() -> h.reserveInventory.reserveInventory(operator, mouse.getId(), warehouse.getId(), 1))
                    .isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void reservingADigitalProductIsRejected() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.reserveInventory.reserveInventory(operator, ebook.getId(), warehouse.getId(), 1))
                    .isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void reservingWithoutInventoryRecordIsReported() {
            assertThatThrownBy(() -> h.reserveInventory.reserveInventory(operator, mouse.getId(), warehouse.getId(), 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void reservingInAnUnknownWarehouseIsReported() {
            assertThatThrownBy(() -> h.reserveInventory.reserveInventory(operator, mouse.getId(), "missing", 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void buyerCannotReserveDirectly() {
            h.stock(mouse, warehouse, 10);

            assertThatThrownBy(() -> h.reserveInventory.reserveInventory(h.buyer(), mouse.getId(), warehouse.getId(), 1))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void releasingAReservationReturnsTheStock() {
            h.stock(mouse, warehouse, 10);
            h.reserveInventory.reserveInventory(operator, mouse.getId(), warehouse.getId(), 4);

            Inventory inventory = h.releaseInventoryReservation.releaseInventoryReservation(operator, mouse.getId(),
                    warehouse.getId(), 3);

            assertThat(inventory.getQuantity()).isEqualTo(9);
            assertThat(inventory.getReservedQuantity()).isEqualTo(1);
            assertThat(h.audit.actions()).contains(AuditAction.INVENTORY_RESERVATION_RELEASED);
        }

        @Test
        void releasingMoreThanReservedIsRejected() {
            h.stock(mouse, warehouse, 10);

            assertThatThrownBy(() -> h.releaseInventoryReservation.releaseInventoryReservation(operator, mouse.getId(),
                    warehouse.getId(), 1)).isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void releasingForADigitalProductIsRejected() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.releaseInventoryReservation.releaseInventoryReservation(operator, ebook.getId(),
                    warehouse.getId(), 1)).isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void releasingWithoutInventoryRecordIsReported() {
            assertThatThrownBy(() -> h.releaseInventoryReservation.releaseInventoryReservation(operator, mouse.getId(),
                    warehouse.getId(), 1)).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ConsultInventory {

        @Test
        void sellerConsultsTheInventoryOfItsProduct() {
            Inventory inventory = h.stock(mouse, warehouse, 10);

            assertThat(h.consultInventory.consultProductInventory(seller, mouse.getId())).containsExactly(inventory);
        }

        @Test
        void sellerCannotConsultTheInventoryOfAnotherSellersProduct() {
            assertThatThrownBy(() -> h.consultInventory.consultProductInventory(h.seller(), mouse.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void sellerConsultsTheInventoryOfItsWarehouse() {
            Inventory inventory = h.stock(mouse, warehouse, 10);

            assertThat(h.consultInventory.consultWarehouseInventory(seller, warehouse.getId())).containsExactly(inventory);
        }

        @Test
        void sellerCannotConsultAMarketplaceWarehouse() {
            Warehouse hub = h.marketplaceWarehouse();

            assertThatThrownBy(() -> h.consultInventory.consultWarehouseInventory(seller, hub.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void supervisorConsultsAnyWarehouse() {
            h.stock(mouse, warehouse, 10);

            assertThat(h.consultInventory.consultWarehouseInventory(h.supervisor(), warehouse.getId())).hasSize(1);
        }

        @Test
        void digitalProductHasNoInventoryToConsult() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            assertThatThrownBy(() -> h.consultInventory.consultProductInventory(seller, ebook.getId()))
                    .isInstanceOf(InvalidInventoryException.class);
        }

        @Test
        void buyerIsNotAllowedToConsultInventory() {
            assertThatThrownBy(() -> h.consultInventory.consultProductInventory(h.buyer(), mouse.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void unknownWarehouseIsReported() {
            assertThatThrownBy(() -> h.consultInventory.consultWarehouseInventory(operator, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
