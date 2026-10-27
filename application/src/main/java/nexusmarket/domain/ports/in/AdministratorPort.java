package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Role input port ({@code ADMINISTRATOR}).
 *
 * <p>Administration of users, sellers, buyers' commercial status, Marketplace warehouses, returns, refunds, audit logs and administrative reports.</p>
 */
public interface AdministratorPort {

    User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber,
                           String fullName, String email, String rawPassword);

    User consultUser(User requestingUser, String userId);

    User changeUserStatus(User requestingUser, String userId, UserStatus newStatus);

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);

    Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email,
                          String rawPassword, String warehouseName, Address warehouseLocation);

    Seller consultSeller(User requestingUser, String sellerId);

    Seller updateSeller(User requestingUser, String sellerId, String fullName);

    Buyer consultBuyer(User requestingUser, String buyerId);

    Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus);

    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);

    Warehouse consultWarehouse(User requestingUser, String warehouseId);

    List<Warehouse> consultWarehouses(User requestingUser);

    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);

    List<Product> consultSellerProducts(User requestingUser, String sellerId);

    Invoice consultInvoice(User requestingUser, String orderId);

    ReturnRequest approveReturn(User requestingUser, String returnRequestId);

    ReturnRequest rejectReturn(User requestingUser, String returnRequestId);

    Refund processRefund(User requestingUser, String refundId);

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
