package nexusmarket.domain.services.support;

import static nexusmarket.domain.DomainFixtures.id;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.audit.ConsultAuditLogsService;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateUserStatusService;
import nexusmarket.domain.services.buyer.ChangeBuyerCommercialStatusService;
import nexusmarket.domain.services.buyer.ConsultBuyerService;
import nexusmarket.domain.services.buyer.UpdateBuyerProfileService;
import nexusmarket.domain.services.cart.AddItemToCartService;
import nexusmarket.domain.services.cart.ClearCartService;
import nexusmarket.domain.services.cart.ConsultCartService;
import nexusmarket.domain.services.cart.RemoveItemFromCartService;
import nexusmarket.domain.services.cart.UpdateCartItemQuantityService;
import nexusmarket.domain.services.inventory.ConsultInventoryService;
import nexusmarket.domain.services.inventory.RegisterInitialStockService;
import nexusmarket.domain.services.inventory.RegisterInventoryAdjustmentService;
import nexusmarket.domain.services.inventory.RegisterInventoryInboundService;
import nexusmarket.domain.services.inventory.ReleaseInventoryReservationService;
import nexusmarket.domain.services.inventory.ReserveInventoryService;
import nexusmarket.domain.services.invoice.ConsultInvoiceService;
import nexusmarket.domain.services.invoice.GenerateInvoiceService;
import nexusmarket.domain.services.order.ConfirmOrderService;
import nexusmarket.domain.services.order.ConsultBuyerOrdersService;
import nexusmarket.domain.services.order.ConsultOrderService;
import nexusmarket.domain.services.order.ValidatePaymentService;
import nexusmarket.domain.services.product.ConsultCatalogService;
import nexusmarket.domain.services.product.ConsultSellerProductsService;
import nexusmarket.domain.services.product.DiscontinueProductService;
import nexusmarket.domain.services.product.PublishProductService;
import nexusmarket.domain.services.product.RegisterProductService;
import nexusmarket.domain.services.product.SuspendProductService;
import nexusmarket.domain.services.product.UpdateProductService;
import nexusmarket.domain.services.reporting.ConsultCommercialReportService;
import nexusmarket.domain.services.reporting.ConsultInventoryReportService;
import nexusmarket.domain.services.reporting.ConsultLogisticsReportService;
import nexusmarket.domain.services.reporting.ConsultSellerPerformanceReportService;
import nexusmarket.domain.services.returns.ApproveReturnService;
import nexusmarket.domain.services.returns.ConsultReturnService;
import nexusmarket.domain.services.returns.ProcessRefundService;
import nexusmarket.domain.services.returns.RejectReturnService;
import nexusmarket.domain.services.returns.RequestReturnService;
import nexusmarket.domain.services.seller.ConsultSellerService;
import nexusmarket.domain.services.seller.RegisterSellerService;
import nexusmarket.domain.services.seller.UpdateSellerService;
import nexusmarket.domain.services.shipment.ConfirmDeliveryService;
import nexusmarket.domain.services.shipment.CreateShipmentsService;
import nexusmarket.domain.services.shipment.DispatchShipmentService;
import nexusmarket.domain.services.shipment.PackShipmentService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ChangeUserStatusService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.services.user.LoadAuthenticatedUserService;
import nexusmarket.domain.services.user.LoginService;
import nexusmarket.domain.services.user.LogoutService;
import nexusmarket.domain.services.user.RegisterBuyerUserService;
import nexusmarket.domain.services.user.RegisterStaffUserService;
import nexusmarket.domain.services.warehouse.ConsultWarehouseService;
import nexusmarket.domain.services.warehouse.RegisterWarehouseService;
import nexusmarket.domain.services.warehouse.UpdateWarehouseService;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Wires the 60 domain services with in-memory output ports, without Spring (like the banking
 * reference Harness), and builds common scenarios. A new harness per test gives an empty "database".
 */
public class ServiceHarness {

    // ------------------------------------------------------------------ output ports

    public final InMemoryUserRepository users = new InMemoryUserRepository();
    public final InMemoryBuyerRepository buyers = new InMemoryBuyerRepository(users);
    public final InMemorySellerRepository sellers = new InMemorySellerRepository(users);
    public final InMemoryWarehouseRepository warehouses = new InMemoryWarehouseRepository();
    public final InMemoryProductRepository products = new InMemoryProductRepository();
    public final InMemoryInventoryRepository inventories = new InMemoryInventoryRepository();
    public final InMemoryCartRepository carts = new InMemoryCartRepository();
    public final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    public final InMemoryInvoiceRepository invoices = new InMemoryInvoiceRepository();
    public final InMemoryShipmentRepository shipments = new InMemoryShipmentRepository();
    public final InMemoryReturnRequestRepository returnRequests = new InMemoryReturnRequestRepository();
    public final InMemoryRefundRepository refunds = new InMemoryRefundRepository();
    public final InMemoryAuditLogRepository audit = new InMemoryAuditLogRepository();
    public final FakePasswordService passwords = new FakePasswordService();
    public final FakeJwtService jwt = new FakeJwtService();
    public final FakePaymentGateway payments = new FakePaymentGateway();

    // ------------------------------------------------------------------ internal services

    public final RegisterAuditLogService registerAuditLog = new RegisterAuditLogService(audit);
    public final ValidateUserStatusService validateUserStatus = new ValidateUserStatusService(users);
    public final ValidateRoleAuthorizationService validateRole = new ValidateRoleAuthorizationService(validateUserStatus);
    public final ValidateBuyerOwnershipService buyerOwnership = new ValidateBuyerOwnershipService();
    public final ValidateSellerOwnershipService sellerOwnership = new ValidateSellerOwnershipService();
    public final GenerateInvoiceService generateInvoice = new GenerateInvoiceService(invoices, registerAuditLog);
    public final ReserveInventoryService reserveInventory = new ReserveInventoryService(products, warehouses,
            inventories, validateRole, registerAuditLog);

    // ------------------------------------------------------------------ services by subdomain

    public final ConsultAuditLogsService consultAuditLogs = new ConsultAuditLogsService(audit, users, validateRole);

    public final RegisterBuyerUserService registerBuyerUser = new RegisterBuyerUserService(users, buyers, carts,
            passwords, registerAuditLog);
    public final RegisterStaffUserService registerStaffUser = new RegisterStaffUserService(users, passwords,
            validateRole, registerAuditLog);
    public final LoginService login = new LoginService(users, passwords, jwt, registerAuditLog);
    public final LogoutService logout = new LogoutService(users, registerAuditLog);
    public final LoadAuthenticatedUserService loadAuthenticatedUser = new LoadAuthenticatedUserService(users);
    public final ConsultUserService consultUser = new ConsultUserService(users, validateRole);
    public final ChangeUserStatusService changeUserStatus = new ChangeUserStatusService(users, validateRole,
            registerAuditLog);
    public final ChangeUserPasswordService changeUserPassword = new ChangeUserPasswordService(users, passwords,
            validateRole, registerAuditLog);

    public final RegisterSellerService registerSeller = new RegisterSellerService(users, sellers, warehouses,
            passwords, validateRole, registerAuditLog);
    public final ConsultSellerService consultSeller = new ConsultSellerService(sellers, validateRole, sellerOwnership);
    public final UpdateSellerService updateSeller = new UpdateSellerService(sellers, validateRole, sellerOwnership,
            registerAuditLog);

    public final ConsultBuyerService consultBuyer = new ConsultBuyerService(buyers, validateRole, buyerOwnership);
    public final UpdateBuyerProfileService updateBuyerProfile = new UpdateBuyerProfileService(buyers, validateRole,
            registerAuditLog);
    public final ChangeBuyerCommercialStatusService changeBuyerCommercialStatus =
            new ChangeBuyerCommercialStatusService(buyers, validateRole, registerAuditLog);

    public final RegisterWarehouseService registerWarehouse = new RegisterWarehouseService(sellers, warehouses,
            validateRole, registerAuditLog);
    public final ConsultWarehouseService consultWarehouse = new ConsultWarehouseService(warehouses, sellers,
            validateRole, sellerOwnership);
    public final UpdateWarehouseService updateWarehouse = new UpdateWarehouseService(warehouses, validateRole,
            sellerOwnership, registerAuditLog);

    public final RegisterProductService registerProduct = new RegisterProductService(sellers, products, validateRole,
            registerAuditLog);
    public final UpdateProductService updateProduct = new UpdateProductService(products, validateRole,
            sellerOwnership, registerAuditLog);
    public final PublishProductService publishProduct = new PublishProductService(products, validateRole,
            sellerOwnership, registerAuditLog);
    public final SuspendProductService suspendProduct = new SuspendProductService(products, validateRole,
            sellerOwnership, registerAuditLog);
    public final DiscontinueProductService discontinueProduct = new DiscontinueProductService(products, validateRole,
            sellerOwnership, registerAuditLog);
    public final ConsultCatalogService consultCatalog = new ConsultCatalogService(products);
    public final ConsultSellerProductsService consultSellerProducts = new ConsultSellerProductsService(sellers,
            products, validateRole, sellerOwnership);

    public final RegisterInitialStockService registerInitialStock = new RegisterInitialStockService(products,
            warehouses, inventories, validateRole, sellerOwnership, registerAuditLog);
    public final RegisterInventoryInboundService registerInventoryInbound = new RegisterInventoryInboundService(
            products, warehouses, inventories, validateRole, sellerOwnership, registerAuditLog);
    public final RegisterInventoryAdjustmentService registerInventoryAdjustment =
            new RegisterInventoryAdjustmentService(products, warehouses, inventories, validateRole, registerAuditLog);
    public final ReleaseInventoryReservationService releaseInventoryReservation =
            new ReleaseInventoryReservationService(products, warehouses, inventories, validateRole, registerAuditLog);
    public final ConsultInventoryService consultInventory = new ConsultInventoryService(products, warehouses,
            inventories, validateRole, sellerOwnership);

    public final AddItemToCartService addItemToCart = new AddItemToCartService(buyers, carts, products, validateRole,
            registerAuditLog);
    public final RemoveItemFromCartService removeItemFromCart = new RemoveItemFromCartService(buyers, carts, products,
            validateRole, registerAuditLog);
    public final UpdateCartItemQuantityService updateCartItemQuantity = new UpdateCartItemQuantityService(buyers,
            carts, products, validateRole, registerAuditLog);
    public final ConsultCartService consultCart = new ConsultCartService(buyers, carts, validateRole, buyerOwnership);
    public final ClearCartService clearCart = new ClearCartService(buyers, carts, validateRole, registerAuditLog);

    public final ConfirmOrderService confirmOrder = new ConfirmOrderService(buyers, carts, products, inventories,
            orders, validateRole, reserveInventory, registerAuditLog);
    public final ValidatePaymentService validatePayment = new ValidatePaymentService(orders, payments, validateRole,
            buyerOwnership, generateInvoice, registerAuditLog);
    public final ConsultOrderService consultOrder = new ConsultOrderService(orders, validateRole, buyerOwnership);
    public final ConsultBuyerOrdersService consultBuyerOrders = new ConsultBuyerOrdersService(buyers, orders,
            validateRole, buyerOwnership);

    public final ConsultInvoiceService consultInvoice = new ConsultInvoiceService(orders, invoices, validateRole,
            buyerOwnership);

    public final CreateShipmentsService createShipments = new CreateShipmentsService(orders, shipments, validateRole,
            registerAuditLog);
    public final PackShipmentService packShipment = new PackShipmentService(shipments, validateRole, registerAuditLog);
    public final DispatchShipmentService dispatchShipment = new DispatchShipmentService(shipments, orders, inventories,
            validateRole, registerAuditLog);
    public final ConfirmDeliveryService confirmDelivery = new ConfirmDeliveryService(shipments, orders, validateRole,
            registerAuditLog);

    public final RequestReturnService requestReturn = new RequestReturnService(orders, returnRequests, validateRole,
            buyerOwnership, registerAuditLog);
    public final ApproveReturnService approveReturn = new ApproveReturnService(returnRequests, invoices, inventories,
            refunds, validateRole, registerAuditLog);
    public final RejectReturnService rejectReturn = new RejectReturnService(returnRequests, validateRole,
            registerAuditLog);
    public final ProcessRefundService processRefund = new ProcessRefundService(refunds, validateRole,
            registerAuditLog);
    public final ConsultReturnService consultReturn = new ConsultReturnService(returnRequests, refunds, validateRole,
            buyerOwnership);

    public final ConsultCommercialReportService commercialReport = new ConsultCommercialReportService(orders,
            invoices, refunds, validateRole, registerAuditLog);
    public final ConsultSellerPerformanceReportService sellerPerformanceReport =
            new ConsultSellerPerformanceReportService(sellers, products, orders, returnRequests, validateRole,
                    sellerOwnership, registerAuditLog);
    public final ConsultInventoryReportService inventoryReport = new ConsultInventoryReportService(warehouses,
            inventories, validateRole, registerAuditLog);
    public final ConsultLogisticsReportService logisticsReport = new ConsultLogisticsReportService(shipments,
            validateRole, registerAuditLog);

    // ------------------------------------------------------------------ actors (stored)

    public Administrator admin() {
        return (Administrator) users.save(DomainFixtures.administrator());
    }

    public Supervisor supervisor() {
        return (Supervisor) users.save(DomainFixtures.supervisor());
    }

    public LogisticsOperator operator() {
        return (LogisticsOperator) users.save(DomainFixtures.operator());
    }

    /** A stored buyer with its own (empty) cart. */
    public Buyer buyer() {
        Buyer buyer = DomainFixtures.buyer();
        Cart cart = new Cart(id("cart"), buyer);
        buyer.assignCart(cart);
        buyers.save(buyer);
        carts.save(cart);
        return buyer;
    }

    /** A stored seller with one stored SELLER warehouse. */
    public Seller seller() {
        Seller seller = DomainFixtures.seller();
        sellers.save(seller);
        warehouseOf(seller);
        return seller;
    }

    public Warehouse warehouseOf(Seller seller) {
        Warehouse warehouse = DomainFixtures.sellerWarehouse(seller);
        seller.addWarehouse(warehouse);
        return warehouses.save(warehouse);
    }

    public Warehouse firstWarehouse(Seller seller) {
        return seller.getWarehouses().get(0);
    }

    public Warehouse marketplaceWarehouse() {
        return warehouses.save(DomainFixtures.marketplaceWarehouse());
    }

    /** Stores the user after changing its status to BLOCKED. */
    public <T extends User> T blocked(T user) {
        user.changeStatus(UserStatus.BLOCKED);
        users.update(user);
        return user;
    }

    // ------------------------------------------------------------------ catalog and stock (stored)

    public PhysicalProduct physicalProduct(Seller seller, String price, Variant... variants) {
        return (PhysicalProduct) products.save(new PhysicalProduct(id("product"), "Wireless Mouse", "Ergonomic mouse",
                new BigDecimal(price), seller, List.of(variants), ProductStatus.PUBLISHED));
    }

    public PhysicalProduct productWithStatus(Seller seller, ProductStatus status) {
        return (PhysicalProduct) products.save(new PhysicalProduct(id("product"), "Wireless Mouse", "Ergonomic mouse",
                new BigDecimal("100.00"), seller, List.of(), status));
    }

    public DigitalProduct digitalProduct(Seller seller, String price) {
        return (DigitalProduct) products.save(new DigitalProduct(id("product"), "E-book", "Java guide",
                new BigDecimal(price), seller, List.of(), ProductStatus.PUBLISHED));
    }

    public Inventory stock(PhysicalProduct product, Warehouse warehouse, int quantity) {
        return inventories.save(new Inventory(product, warehouse, quantity));
    }

    public Inventory inventoryOf(PhysicalProduct product, Warehouse warehouse) {
        return inventories.findByProductAndWarehouse(product, warehouse).orElseThrow();
    }

    // ------------------------------------------------------------------ purchase flow through the services

    public Order confirmedOrder(Buyer buyer, Product product, int quantity) {
        addItemToCart.addItemToCart(buyer, product.getId(), null, quantity);
        return confirmOrder.confirmOrder(buyer);
    }

    public Order paidOrder(Buyer buyer, Product product, int quantity) {
        Order order = confirmedOrder(buyer, product, quantity);
        return validatePayment.validatePayment(buyer, order.getId());
    }

    /** A paid physical order shipped and delivered by the given operator. */
    public Order deliveredOrder(Buyer buyer, PhysicalProduct product, int quantity, LogisticsOperator operator) {
        Order order = paidOrder(buyer, product, quantity);
        for (Shipment shipment : createShipments.createShipments(operator, order.getId())) {
            packShipment.packShipment(operator, shipment.getId());
            dispatchShipment.dispatchShipment(operator, shipment.getId());
            confirmDelivery.confirmDelivery(operator, shipment.getId());
        }
        return orders.findById(order.getId()).orElseThrow();
    }
}
