package nexusmarket.domain.models;

import nexusmarket.domain.exceptions.InvalidReportParameterException;

/**
 * LowStockProduct (read-only).
 *
 * <p>Detail row of an {@link InventoryReport}: a physical product whose
 * available stock, summed over every warehouse and excluding damaged records,
 * is below the low-stock threshold.</p>
 */
public class LowStockProduct {

    private final PhysicalProduct product;
    private final int availableUnits;

    public LowStockProduct(PhysicalProduct product, int availableUnits) {
        if (product == null) {
            throw new InvalidReportParameterException("Low stock product is required.");
        }
        if (availableUnits < 0) {
            throw new InvalidReportParameterException("Low stock available units cannot be negative.");
        }
        this.product = product;
        this.availableUnits = availableUnits;
    }

    public PhysicalProduct getProduct() {
        return product;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    @Override
    public String toString() {
        return "LowStockProduct{product=" + product.getId() + ", availableUnits=" + availableUnits + "}";
    }
}
