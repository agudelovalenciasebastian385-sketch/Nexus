package nexusmarket.adapters.useCases;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.services.inventory.ConsultInventoryService;
import nexusmarket.domain.services.inventory.RegisterInitialStockService;
import nexusmarket.domain.services.inventory.RegisterInventoryInboundService;
import nexusmarket.domain.services.order.ConsultOrderService;
import nexusmarket.domain.services.product.ConsultSellerProductsService;
import nexusmarket.domain.services.product.DiscontinueProductService;
import nexusmarket.domain.services.product.PublishProductService;
import nexusmarket.domain.services.product.RegisterProductService;
import nexusmarket.domain.services.product.SuspendProductService;
import nexusmarket.domain.services.product.UpdateProductService;
import nexusmarket.domain.services.reporting.ConsultSellerPerformanceReportService;
import nexusmarket.domain.services.seller.ConsultSellerService;
import nexusmarket.domain.services.seller.UpdateSellerService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.services.warehouse.ConsultWarehouseService;
import nexusmarket.domain.services.warehouse.RegisterWarehouseService;
import nexusmarket.domain.services.warehouse.UpdateWarehouseService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.Variant;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link SellerPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class SellerUseCaseImpl implements SellerPort {

    private final ConsultUserService consultUserService;
    private final ChangeUserPasswordService changeUserPasswordService;
    private final ConsultSellerService consultSellerService;
    private final UpdateSellerService updateSellerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ConsultWarehouseService consultWarehouseService;
    private final UpdateWarehouseService updateWarehouseService;
    private final RegisterProductService registerProductService;
    private final UpdateProductService updateProductService;
    private final PublishProductService publishProductService;
    private final SuspendProductService suspendProductService;
    private final DiscontinueProductService discontinueProductService;
    private final ConsultSellerProductsService consultSellerProductsService;
    private final RegisterInitialStockService registerInitialStockService;
    private final RegisterInventoryInboundService registerInventoryInboundService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultOrderService consultOrderService;
    private final ConsultSellerPerformanceReportService consultSellerPerformanceReportService;

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
    public Seller updateSeller(User requestingUser, String sellerId, String fullName) {
        return updateSellerService.updateSeller(requestingUser, sellerId, fullName);
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
    public Product registerProduct(User requestingUser, ProductType type, String name, String description,
                                   BigDecimal price, List<Variant> variants) {
        return registerProductService.registerProduct(requestingUser, type, name, description, price, variants);
    }

    @Override
    public Product updateProduct(User requestingUser, String productId, String name, String description,
                                 BigDecimal price, List<Variant> variants) {
        return updateProductService.updateProduct(requestingUser, productId, name, description, price, variants);
    }

    @Override
    public Product publishProduct(User requestingUser, String productId) {
        return publishProductService.publishProduct(requestingUser, productId);
    }

    @Override
    public Product suspendProduct(User requestingUser, String productId) {
        return suspendProductService.suspendProduct(requestingUser, productId);
    }

    @Override
    public Product discontinueProduct(User requestingUser, String productId) {
        return discontinueProductService.discontinueProduct(requestingUser, productId);
    }

    @Override
    public List<Product> consultSellerProducts(User requestingUser, String sellerId) {
        return consultSellerProductsService.consultSellerProducts(requestingUser, sellerId);
    }

    @Override
    public Inventory registerInitialStock(User requestingUser, String productId, String warehouseId,
                                          int quantity) {
        return registerInitialStockService.registerInitialStock(requestingUser, productId, warehouseId, quantity);
    }

    @Override
    public Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                              int quantity) {
        return registerInventoryInboundService.registerInventoryInbound(requestingUser, productId, warehouseId, quantity);
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
    public Order consultOrder(User requestingUser, String orderId) {
        return consultOrderService.consultOrder(requestingUser, orderId);
    }

    @Override
    public SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId,
                                                                  ReportPeriod period) {
        return consultSellerPerformanceReportService.consultSellerPerformanceReport(requestingUser, sellerId, period);
    }
}
