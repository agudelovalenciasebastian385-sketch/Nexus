package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.SupervisorPort;
import nexusmarket.domain.services.audit.ConsultAuditLogsService;
import nexusmarket.domain.services.buyer.ConsultBuyerService;
import nexusmarket.domain.services.cart.ConsultCartService;
import nexusmarket.domain.services.inventory.ConsultInventoryService;
import nexusmarket.domain.services.invoice.ConsultInvoiceService;
import nexusmarket.domain.services.order.ConsultBuyerOrdersService;
import nexusmarket.domain.services.order.ConsultOrderService;
import nexusmarket.domain.services.product.ConsultSellerProductsService;
import nexusmarket.domain.services.returns.ConsultReturnService;
import nexusmarket.domain.services.seller.ConsultSellerService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.services.warehouse.ConsultWarehouseService;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link SupervisorPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final ConsultUserService consultUserService;
    private final ChangeUserPasswordService changeUserPasswordService;
    private final ConsultSellerService consultSellerService;
    private final ConsultBuyerService consultBuyerService;
    private final ConsultWarehouseService consultWarehouseService;
    private final ConsultSellerProductsService consultSellerProductsService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultCartService consultCartService;
    private final ConsultOrderService consultOrderService;
    private final ConsultBuyerOrdersService consultBuyerOrdersService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultReturnService consultReturnService;
    private final ConsultAuditLogsService consultAuditLogsService;

    @Override
    public User consultUser(User requestingUser, String userId) {
        return consultUserService.consultUser(requestingUser, userId);
    }

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        changeUserPasswordService.changeUserPassword(requestingUser, currentPassword, newPassword);
    }

    @Override
    public Seller consultSeller(User requestingUser, String sellerId) {
        return consultSellerService.consultSeller(requestingUser, sellerId);
    }

    @Override
    public Buyer consultBuyer(User requestingUser, String buyerId) {
        return consultBuyerService.consultBuyer(requestingUser, buyerId);
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
    public List<Product> consultSellerProducts(User requestingUser, String sellerId) {
        return consultSellerProductsService.consultSellerProducts(requestingUser, sellerId);
    }

    @Override
    public List<Inventory> consultProductInventory(User requestingUser, String productId) {
        return consultInventoryService.consultProductInventory(requestingUser, productId);
    }

    @Override
    public List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId) {
        return consultInventoryService.consultWarehouseInventory(requestingUser, warehouseId);
    }

    @Override
    public Cart consultCart(User requestingUser, String buyerId) {
        return consultCartService.consultCart(requestingUser, buyerId);
    }

    @Override
    public Order consultOrder(User requestingUser, String orderId) {
        return consultOrderService.consultOrder(requestingUser, orderId);
    }

    @Override
    public List<Order> consultBuyerOrders(User requestingUser, String buyerId) {
        return consultBuyerOrdersService.consultBuyerOrders(requestingUser, buyerId);
    }

    @Override
    public Invoice consultInvoice(User requestingUser, String orderId) {
        return consultInvoiceService.consultInvoice(requestingUser, orderId);
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
