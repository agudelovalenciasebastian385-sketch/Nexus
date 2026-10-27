package nexusmarket.domain.models;

import nexusmarket.domain.exceptions.InvalidReportParameterException;

/**
 * WarehouseStockSummary (read-only).
 *
 * <p>Detail row of an {@link InventoryReport}: the stock held by one
 * {@link Warehouse}, aggregated over all its inventory records.</p>
 */
public class WarehouseStockSummary {

    private final Warehouse warehouse;
    /** Available plus reserved units. */
    private final int totalUnits;
    private final int reservedUnits;
    /** Available plus reserved units of the records in condition {@code DAMAGED}. */
    private final int damagedUnits;

    public WarehouseStockSummary(Warehouse warehouse, int totalUnits, int reservedUnits, int damagedUnits) {
        if (warehouse == null) {
            throw new InvalidReportParameterException("Warehouse stock summary warehouse is required.");
        }
        if (totalUnits < 0 || reservedUnits < 0 || damagedUnits < 0) {
            throw new InvalidReportParameterException("Warehouse stock summary units cannot be negative.");
        }
        if (reservedUnits > totalUnits || damagedUnits > totalUnits) {
            throw new InvalidReportParameterException(
                    "Reserved and damaged units cannot exceed the total units of the warehouse.");
        }
        this.warehouse = warehouse;
        this.totalUnits = totalUnits;
        this.reservedUnits = reservedUnits;
        this.damagedUnits = damagedUnits;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public int getReservedUnits() {
        return reservedUnits;
    }

    public int getDamagedUnits() {
        return damagedUnits;
    }

    @Override
    public String toString() {
        return "WarehouseStockSummary{warehouse=" + warehouse.getId() + ", totalUnits=" + totalUnits
                + ", reservedUnits=" + reservedUnits + ", damagedUnits=" + damagedUnits + "}";
    }
}
