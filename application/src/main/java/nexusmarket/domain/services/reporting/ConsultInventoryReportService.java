package nexusmarket.domain.services.reporting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.LowStockProduct;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.models.WarehouseStockSummary;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Consolidates the current stock of every warehouse and lists the products
 * with low stock (reporting-services.md - ConsultInventoryReport). Read-only;
 * the consultation is audited.
 */
@Service
@RequiredArgsConstructor
public class ConsultInventoryReportService {

    private static final String REPORT_TYPE = "INVENTORY";

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR,
                SystemRole.SUPERVISOR, SystemRole.LOGISTICS_OPERATOR);
        if (lowStockThreshold <= 0) {
            throw new InvalidReportParameterException("The low stock threshold must be greater than 0.");
        }

        Map<String, int[]> unitsByWarehouse = new LinkedHashMap<>();
        Map<String, Warehouse> warehouses = new LinkedHashMap<>();
        for (Warehouse warehouse : warehouseRepositoryPort.findAll()) {
            warehouses.put(warehouse.getId(), warehouse);
            unitsByWarehouse.put(warehouse.getId(), new int[3]);
        }
        Map<String, PhysicalProduct> products = new LinkedHashMap<>();
        Map<String, Integer> availableByProduct = new LinkedHashMap<>();
        for (Inventory inventory : inventoryRepositoryPort.findAll()) {
            Warehouse warehouse = inventory.getWarehouse();
            warehouses.putIfAbsent(warehouse.getId(), warehouse);
            int[] units = unitsByWarehouse.computeIfAbsent(warehouse.getId(), id -> new int[3]);
            int recordUnits = inventory.getQuantity() + inventory.getReservedQuantity();
            boolean damaged = inventory.getCondition() == InventoryCondition.DAMAGED;
            units[0] += recordUnits;
            units[1] += inventory.getReservedQuantity();
            if (damaged) {
                units[2] += recordUnits;
            }
            PhysicalProduct product = inventory.getProduct();
            products.putIfAbsent(product.getId(), product);
            availableByProduct.merge(product.getId(), damaged ? 0 : inventory.getQuantity(), Integer::sum);
        }

        List<WarehouseStockSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : unitsByWarehouse.entrySet()) {
            int[] units = entry.getValue();
            summaries.add(new WarehouseStockSummary(warehouses.get(entry.getKey()), units[0], units[1], units[2]));
        }
        List<LowStockProduct> lowStockProducts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : availableByProduct.entrySet()) {
            if (entry.getValue() < lowStockThreshold) {
                lowStockProducts.add(new LowStockProduct(products.get(entry.getKey()), entry.getValue()));
            }
        }
        lowStockProducts.sort(Comparator.comparingInt(LowStockProduct::getAvailableUnits));

        InventoryReport report = new InventoryReport(lowStockThreshold, LocalDateTime.now(), summaries,
                lowStockProducts);
        registerAuditLogService.execute(requester, AuditAction.REPORT_CONSULTED, AuditEntityType.REPORT, REPORT_TYPE,
                Map.of("reportType", REPORT_TYPE, "lowStockThreshold", String.valueOf(lowStockThreshold)));
        return report;
    }
}
