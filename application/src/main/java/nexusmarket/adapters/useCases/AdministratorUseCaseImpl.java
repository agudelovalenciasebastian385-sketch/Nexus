package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.services.audit.ConsultAuditLogsService;
import nexusmarket.domain.services.buyer.ChangeBuyerCommercialStatusService;
import nexusmarket.domain.services.buyer.ConsultBuyerService;
import nexusmarket.domain.services.invoice.ConsultInvoiceService;
import nexusmarket.domain.services.product.ConsultSellerProductsService;
import nexusmarket.domain.services.returns.ApproveReturnService;
import nexusmarket.domain.services.returns.ConsultReturnService;
import nexusmarket.domain.services.returns.ProcessRefundService;
import nexusmarket.domain.services.returns.RejectReturnService;
import nexusmarket.domain.services.seller.ConsultSellerService;
import nexusmarket.domain.services.seller.RegisterSellerService;
import nexusmarket.domain.services.seller.UpdateSellerService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ChangeUserStatusService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.services.user.RegisterStaffUserService;
import nexusmarket.domain.services.warehouse.ConsultWarehouseService;
import nexusmarket.domain.services.warehouse.RegisterWarehouseService;
import nexusmarket.domain.services.warehouse.UpdateWarehouseService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link AdministratorPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class AdministratorUseCaseImpl implements AdministratorPort {

    private final RegisterStaffUserService registerStaffUserService;
    private final ConsultUserService consultUserService;
    private final ChangeUserStatusService changeUserStatusService;
    private final ChangeUserPasswordService changeUserPasswordService;
    private final RegisterSellerService registerSellerService;
    private final ConsultSellerService consultSellerService;
    private final UpdateSellerService updateSellerService;
    private final ConsultBuyerService consultBuyerService;
    private final ChangeBuyerCommercialStatusService changeBuyerCommercialStatusService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ConsultWarehouseService consultWarehouseService;
    private final UpdateWarehouseService updateWarehouseService;
    private final ConsultSellerProductsService consultSellerProductsService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ApproveReturnService approveReturnService;
    private final RejectReturnService rejectReturnService;
    private final ProcessRefundService processRefundService;
    private final ConsultReturnService consultReturnService;
    private final ConsultAuditLogsService consultAuditLogsService;

    @Override
    public User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber,
                                  String fullName, String email, String rawPassword) {
        return registerStaffUserService.registerStaffUser(requestingUser, role, identificationNumber, fullName, email, rawPassword);
    }

    @Override
    public User consultUser(User requestingUser, String userId) {
        return consultUserService.consultUser(requestingUser, userId);
    }

    @Override
    public User changeUserStatus(User requestingUser, String userId, UserStatus newStatus) {
        return changeUserStatusService.changeUserStatus(requestingUser, userId, newStatus);
    }

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        changeUserPasswordService.changeUserPassword(requestingUser, currentPassword, newPassword);
    }

    @Override
    public Seller registerSeller(User requestingUser, String identificationNumber, String fullName,
                                 String email, String rawPassword, String warehouseName,
                                 Address warehouseLocation) {
        return registerSellerService.registerSeller(requestingUser, identificationNumber, fullName, email, rawPassword, warehouseName, warehouseLocation);
    }

    @Override
    public Seller consultSeller(User requestingUser, String sellerId) {
        return consultSellerService.consultSeller(requestingUser, sellerId);
    }

    @Override
    public Seller updateSeller(User requestingUser, String sellerId, String fullName) {
        return updateSellerService.updateSeller(requestingUser, sellerId, fullName);
    }

    @Override
    public Buyer consultBuyer(User requestingUser, String buyerId) {
        return consultBuyerService.consultBuyer(requestingUser, buyerId);
    }

    @Override
    public Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId,
                                             CommercialStatus newStatus) {
        return changeBuyerCommercialStatusService.changeBuyerCommercialStatus(requestingUser, buyerId, newStatus);
    }

    @Override
    public Warehouse registerWarehouse(User requestingUser, String name, Address location,
                                       WarehouseType type) {
        return registerWarehouseService.registerWarehouse(requestingUser, name, location, type);
    }

    @Override
    public Warehouse consultWarehouse(User requestingUser, String warehouseId) {
        return consultWarehouseService.consultWarehouse(requestingUser, warehouseId);
    }

    @Override
    public List<Warehouse> consultWarehouses(User requestingUser) {
        return consultWarehouseService.consultWarehouses(requestingUser);
    }

    @Override
    public Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location) {
        return updateWarehouseService.updateWarehouse(requestingUser, warehouseId, name, location);
    }

    @Override
    public List<Product> consultSellerProducts(User requestingUser, String sellerId) {
        return consultSellerProductsService.consultSellerProducts(requestingUser, sellerId);
    }

    @Override
    public Invoice consultInvoice(User requestingUser, String orderId) {
        return consultInvoiceService.consultInvoice(requestingUser, orderId);
    }

    @Override
    public ReturnRequest approveReturn(User requestingUser, String returnRequestId) {
        return approveReturnService.approveReturn(requestingUser, returnRequestId);
    }

    @Override
    public ReturnRequest rejectReturn(User requestingUser, String returnRequestId) {
        return rejectReturnService.rejectReturn(requestingUser, returnRequestId);
    }

    @Override
    public Refund processRefund(User requestingUser, String refundId) {
        return processRefundService.processRefund(requestingUser, refundId);
    }

    @Override
    public ReturnRequest consultReturn(User requestingUser, String returnRequestId) {
        return consultReturnService.consultReturn(requestingUser, returnRequestId);
    }

    @Override
    public Refund consultRefund(User requestingUser, String returnRequestId) {
        return consultReturnService.consultRefund(requestingUser, returnRequestId);
    }

    @Override
    public List<AuditLog> consultAuditLogs(User requestingUser) {
        return consultAuditLogsService.consultAuditLogs(requestingUser);
    }

    @Override
    public List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId) {
        return consultAuditLogsService.consultAuditLogsByUser(requestingUser, userId);
    }

    @Override
    public List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType,
                                                   String entityId) {
        return consultAuditLogsService.consultAuditLogsByEntity(requestingUser, entityType, entityId);
    }
}
