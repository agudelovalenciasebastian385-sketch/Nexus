package nexusmarket.domain.services.reporting;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.digitalItem;
import static nexusmarket.domain.DomainFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.LowStockProduct;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.models.WarehouseStockSummary;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.RefundStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.ReturnStatus;
import nexusmarket.domain.valueobjects.ShipmentStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The four reports computed from known data. Records are stored directly with fixed dates, so the
 * period boundaries (first instant included, first instant of the next day excluded) are exercised.
 */
class ReportingServicesTest {

    private static final ReportPeriod OCTOBER = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    private static final LocalDateTime FIRST_INSTANT = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime LAST_SECOND = LocalDateTime.of(2026, 10, 31, 23, 59, 59);
    private static final LocalDateTime NEXT_MONTH = LocalDateTime.of(2026, 11, 1, 0, 0);

    private final ServiceHarness h = new ServiceHarness();
    private final Buyer buyer = h.buyer();

    private static LocalDateTime october(int day) {
        return LocalDateTime.of(2026, 10, day, 10, 0);
    }

    private Order order(OrderStatus status, LocalDateTime createdAt, OrderItem... items) {
        return h.orders.save(new Order(id("order"), buyer, List.of(items), status, createdAt, null, null));
    }

    private ReturnRequest returnOf(Order order, ReturnStatus status) {
        return h.returnRequests.save(new ReturnRequest(id("return"), order, "Reason", status));
    }

    @Nested
    class Commercial {

        private final DigitalProduct ebook = h.digitalProduct(h.seller(), "10.00");

        private CommercialReport report() {
            Order paid = order(OrderStatus.PAID, october(5), digitalItem(ebook, 1));
            Order delivered = order(OrderStatus.DELIVERED, LAST_SECOND, digitalItem(ebook, 1));
            order(OrderStatus.PENDING_PAYMENT, FIRST_INSTANT, digitalItem(ebook, 1));
            Order outside = order(OrderStatus.PAID, NEXT_MONTH, digitalItem(ebook, 1));
            h.invoices.save(new Invoice(id("invoice"), paid, new BigDecimal("200.00"), october(5)));
            h.invoices.save(new Invoice(id("invoice"), delivered, new BigDecimal("300.00"), october(31)));
            h.invoices.save(new Invoice(id("invoice"), outside, new BigDecimal("999.00"), NEXT_MONTH));
            h.refunds.save(new Refund(id("refund"), returnOf(delivered, ReturnStatus.APPROVED), new BigDecimal("300.00"),
                    RefundStatus.PROCESSED, october(20)));
            h.refunds.save(new Refund(id("refund"), returnOf(paid, ReturnStatus.APPROVED), new BigDecimal("50.00")));
            return h.commercialReport.consultCommercialReport(h.supervisor(), OCTOBER);
        }

        @Test
        void ordersCreatedInThePeriodAreCountedByStatus() {
            CommercialReport report = report();

            assertThat(report.getTotalOrders()).isEqualTo(3);
            assertThat(report.getOrdersByStatus().get(OrderStatus.PAID)).isEqualTo(1);
            assertThat(report.getOrdersByStatus().get(OrderStatus.DELIVERED)).isEqualTo(1);
            assertThat(report.getOrdersByStatus().get(OrderStatus.PENDING_PAYMENT)).isEqualTo(1);
        }

        @Test
        void totalInvoicedSumsTheInvoicesIssuedInThePeriod() {
            assertThat(report().getTotalInvoiced()).isEqualByComparingTo("500.00");
        }

        @Test
        void totalRefundedSumsOnlyTheRefundsProcessedInThePeriod() {
            assertThat(report().getTotalRefunded()).isEqualByComparingTo("300.00");
        }

        @Test
        void netRevenueIsInvoicedMinusRefunded() {
            assertThat(report().getNetRevenue()).isEqualByComparingTo("200.00");
        }

        @Test
        void consultationIsAudited() {
            report();

            assertThat(h.audit.last(AuditAction.REPORT_CONSULTED).getDetails()).containsEntry("reportType", "COMMERCIAL");
        }

        @Test
        void periodIsRequired() {
            assertThatThrownBy(() -> h.commercialReport.consultCommercialReport(h.admin(), null))
                    .isInstanceOf(InvalidReportPeriodException.class);
        }

        @Test
        void buyerIsNotAllowed() {
            assertThatThrownBy(() -> h.commercialReport.consultCommercialReport(buyer, OCTOBER))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class SellerPerformance {

        private final Seller seller = h.seller();
        private final DigitalProduct mine = h.digitalProduct(seller, "100.00");
        private final DigitalProduct foreign = h.digitalProduct(h.seller(), "50.00");

        SellerPerformance() {
            h.digitalProduct(seller, "20.00");
            h.productWithStatus(seller, ProductStatus.DRAFT);
        }

        private SellerPerformanceReport report() {
            order(OrderStatus.PAID, october(5), digitalItem(mine, 2), digitalItem(foreign, 1));
            Order delivered = order(OrderStatus.DELIVERED, october(10), digitalItem(mine, 3));
            returnOf(delivered, ReturnStatus.APPROVED);
            order(OrderStatus.PENDING_PAYMENT, october(12), digitalItem(mine, 5));
            order(OrderStatus.PAID, october(15), digitalItem(foreign, 4));
            order(OrderStatus.DELIVERED, NEXT_MONTH, digitalItem(mine, 7));
            return h.sellerPerformanceReport.consultSellerPerformanceReport(seller, seller.getId(), OCTOBER);
        }

        @Test
        void onlySoldOrdersWithTheSellersProductsAreCounted() {
            assertThat(report().getOrdersWithSellerProducts()).isEqualTo(2);
        }

        @Test
        void unitsAndAmountCountOnlyTheSellersItems() {
            SellerPerformanceReport report = report();

            assertThat(report.getUnitsSold()).isEqualTo(5);
            assertThat(report.getAmountSold()).isEqualByComparingTo("500.00");
        }

        @Test
        void returnRateIsApprovedReturnsOverOrdersWithTwoDecimals() {
            SellerPerformanceReport report = report();

            assertThat(report.getApprovedReturns()).isEqualTo(1);
            assertThat(report.getReturnRate()).isEqualByComparingTo("50.00");
        }

        @Test
        void currentProductsAreCountedByStatus() {
            SellerPerformanceReport report = report();

            assertThat(report.getProductsByStatus().get(ProductStatus.PUBLISHED)).isEqualTo(2);
            assertThat(report.getProductsByStatus().get(ProductStatus.DRAFT)).isEqualTo(1);
        }

        @Test
        void sellerWithoutSalesHasAZeroReturnRate() {
            SellerPerformanceReport report = h.sellerPerformanceReport.consultSellerPerformanceReport(seller,
                    seller.getId(), OCTOBER);

            assertThat(report.getReturnRate()).isEqualByComparingTo("0.00");
        }

        @Test
        void sellerCannotConsultAnotherSellersReport() {
            assertThatThrownBy(() -> h.sellerPerformanceReport.consultSellerPerformanceReport(h.seller(),
                    seller.getId(), OCTOBER)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void administratorConsultsAnySellersReport() {
            assertThat(h.sellerPerformanceReport.consultSellerPerformanceReport(h.admin(), seller.getId(), OCTOBER)
                    .getSeller()).isEqualTo(seller);
        }

        @Test
        void unknownSellerIsReported() {
            assertThatThrownBy(() -> h.sellerPerformanceReport.consultSellerPerformanceReport(h.admin(), "missing",
                    OCTOBER)).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void periodIsRequired() {
            assertThatThrownBy(() -> h.sellerPerformanceReport.consultSellerPerformanceReport(seller, seller.getId(),
                    null)).isInstanceOf(InvalidReportPeriodException.class);
        }
    }

    @Nested
    class InventoryStock {

        private final Seller seller = h.seller();
        private final Warehouse sellerWarehouse = h.firstWarehouse(seller);
        private final Warehouse hub = h.marketplaceWarehouse();
        private final Warehouse emptyHub = h.marketplaceWarehouse();
        private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
        private final PhysicalProduct lamp = h.physicalProduct(seller, "50.00");
        private final PhysicalProduct cable = h.physicalProduct(seller, "5.00");

        InventoryStock() {
            h.inventories.save(new Inventory(mouse, sellerWarehouse, 8, 2, InventoryCondition.AVAILABLE, null));
            h.inventories.save(new Inventory(lamp, sellerWarehouse, 3, 0, InventoryCondition.DAMAGED, null));
            h.inventories.save(new Inventory(mouse, hub, 1, 0, InventoryCondition.AVAILABLE, null));
            h.inventories.save(new Inventory(cable, hub, 2, 0, InventoryCondition.AVAILABLE, null));
        }

        private InventoryReport report() {
            return h.inventoryReport.consultInventoryReport(h.operator(), 5);
        }

        private WarehouseStockSummary summaryOf(InventoryReport report, Warehouse warehouse) {
            return report.getWarehouses().stream().filter(summary -> summary.getWarehouse().equals(warehouse))
                    .findFirst().orElseThrow();
        }

        @Test
        void warehouseTotalsIncludeAvailableAndReservedUnits() {
            WarehouseStockSummary summary = summaryOf(report(), sellerWarehouse);

            assertThat(summary.getTotalUnits()).isEqualTo(13);
            assertThat(summary.getReservedUnits()).isEqualTo(2);
            assertThat(summary.getDamagedUnits()).isEqualTo(3);
        }

        @Test
        void warehouseWithoutInventoryIsReportedWithZeroUnits() {
            assertThat(summaryOf(report(), emptyHub).getTotalUnits()).isZero();
        }

        @Test
        void productsBelowTheThresholdAreListedFromTheLowestAvailable() {
            assertThat(report().getLowStockProducts()).extracting(LowStockProduct::getProduct)
                    .containsExactly(lamp, cable);
        }

        @Test
        void damagedStockDoesNotCountAsAvailable() {
            LowStockProduct lampEntry = report().getLowStockProducts().get(0);

            assertThat(lampEntry.getAvailableUnits()).isZero();
        }

        @Test
        void availableUnitsAreAddedAcrossWarehouses() {
            assertThat(report().getLowStockProducts()).extracting(LowStockProduct::getProduct).doesNotContain(mouse);
        }

        @Test
        void consultationIsAudited() {
            report();

            assertThat(h.audit.last(AuditAction.REPORT_CONSULTED).getDetails()).containsEntry("lowStockThreshold", "5");
        }

        @Test
        void thresholdMustBeGreaterThanZero() {
            assertThatThrownBy(() -> h.inventoryReport.consultInventoryReport(h.admin(), 0))
                    .isInstanceOf(InvalidReportParameterException.class);
        }

        @Test
        void sellerIsNotAllowed() {
            assertThatThrownBy(() -> h.inventoryReport.consultInventoryReport(seller, 5))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class Logistics {

        private final LogisticsOperator operator = h.operator();
        private final Order order = DomainFixtures.physicalOrder(OrderStatus.SHIPPED);
        private final Warehouse warehouse = h.marketplaceWarehouse();

        private void shipment(ShipmentStatus status, LocalDateTime createdAt) {
            h.shipments.save(new Shipment(id("shipment"), order, warehouse, ADDRESS, operator, status, createdAt));
        }

        private LogisticsReport report() {
            shipment(ShipmentStatus.PREPARING, FIRST_INSTANT);
            shipment(ShipmentStatus.PACKED, october(3));
            shipment(ShipmentStatus.IN_TRANSIT, october(4));
            shipment(ShipmentStatus.DELIVERED, october(5));
            shipment(ShipmentStatus.DELIVERED, LAST_SECOND);
            shipment(ShipmentStatus.DELIVERED, NEXT_MONTH);
            return h.logisticsReport.consultLogisticsReport(h.admin(), OCTOBER);
        }

        @Test
        void shipmentsCreatedInThePeriodAreCounted() {
            assertThat(report().getTotalShipments()).isEqualTo(5);
        }

        @Test
        void pendingDispatchCountsPreparingAndPackedShipments() {
            assertThat(report().getPendingDispatch()).isEqualTo(2);
        }

        @Test
        void deliveredCountsOnlyTheDeliveriesOfThePeriod() {
            assertThat(report().getDelivered()).isEqualTo(2);
        }

        @Test
        void shipmentsAreGroupedByStatus() {
            assertThat(report().getShipmentsByStatus().get(ShipmentStatus.IN_TRANSIT)).isEqualTo(1);
        }

        @Test
        void periodIsRequired() {
            assertThatThrownBy(() -> h.logisticsReport.consultLogisticsReport(h.admin(), null))
                    .isInstanceOf(InvalidReportPeriodException.class);
        }

        @Test
        void operatorIsNotAllowed() {
            assertThatThrownBy(() -> h.logisticsReport.consultLogisticsReport(operator, OCTOBER))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }
}
