package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReportPeriod;

/**
 * Role input port ({@code SUPERVISOR}).
 *
 * <p>Read-only oversight: consultation operations across all business domains. It never modifies information.</p>
 */
public interface SupervisorPort {

    User consultUser(User requestingUser, String userId);

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);

    Seller consultSeller(User requestingUser, String sellerId);

    Buyer consultBuyer(User requestingUser, String buyerId);

    Warehouse consultWarehouse(User requestingUser, String warehouseId);

    List<Warehouse> consultWarehouses(User requestingUser);

    List<Product> consultSellerProducts(User requestingUser, String sellerId);

    List<Inventory> consultProductInventory(User requestingUser, String productId);

    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);

    Cart consultCart(User requestingUser, String buyerId);

    Order consultOrder(User requestingUser, String orderId);

    List<Order> consultBuyerOrders(User requestingUser, String buyerId);

    Invoice consultInvoice(User requestingUser, String orderId);

    ReturnRequest consultReturn(User requestingUser, String returnRequestId);

    Refund consultRefund(User requestingUser, String returnRequestId);

    List<AuditLog> consultAuditLogs(User requestingUser);

    List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);

    List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);

    CommercialReport consultCommercialReport(User requestingUser, ReportPeriod period);

    SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId,
                                                           ReportPeriod period);

    InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);

    LogisticsReport consultLogisticsReport(User requestingUser, ReportPeriod period);
}
