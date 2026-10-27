package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidReportParameterException;

/**
 * InventoryReport (read-only).
 *
 * <p>Current stock of the Marketplace: one {@link WarehouseStockSummary} per
 * warehouse and the {@link LowStockProduct} list for the requested threshold.
 * It is computed when consulted and never persisted.</p>
 */
public class InventoryReport {

    /** Products whose available stock is strictly below this value are listed as low stock. Greater than 0. */
    private final int lowStockThreshold;
    private final LocalDateTime generatedAt;
    private final List<WarehouseStockSummary> warehouses;
    private final List<LowStockProduct> lowStockProducts;

    public InventoryReport(int lowStockThreshold, LocalDateTime generatedAt, List<WarehouseStockSummary> warehouses,
                           List<LowStockProduct> lowStockProducts) {
        if (lowStockThreshold <= 0) {
            throw new InvalidReportParameterException("The low stock threshold must be greater than 0.");
        }
        if (generatedAt == null) {
            throw new InvalidReportParameterException("Inventory report generatedAt is required.");
        }
        this.lowStockThreshold = lowStockThreshold;
        this.generatedAt = generatedAt;
        this.warehouses = warehouses == null ? List.of() : List.copyOf(warehouses);
        this.lowStockProducts = lowStockProducts == null ? List.of() : List.copyOf(lowStockProducts);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<WarehouseStockSummary> getWarehouses() {
        return warehouses;
    }

    public List<LowStockProduct> getLowStockProducts() {
        return lowStockProducts;
    }

    @Override
    public String toString() {
        return "InventoryReport{lowStockThreshold=" + lowStockThreshold + ", warehouses=" + warehouses.size()
                + ", lowStockProducts=" + lowStockProducts.size() + "}";
    }
}
