package nexusmarket.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.Variant;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Role input port ({@code SELLER}).
 *
 * <p>Operations of a seller over its own profile, warehouses, products, inventory, the orders that contain its products and its own performance report.</p>
 */
public interface SellerPort {

    User consultUser(User requestingUser, String userId);

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);

    Seller consultSeller(User requestingUser, String sellerId);

    Seller updateSeller(User requestingUser, String sellerId, String fullName);

    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);

    Warehouse consultWarehouse(User requestingUser, String warehouseId);

    List<Warehouse> consultWarehouses(User requestingUser);

    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);

    Product registerProduct(User requestingUser, ProductType type, String name, String description,
                            BigDecimal price, List<Variant> variants);

    Product updateProduct(User requestingUser, String productId, String name, String description,
                          BigDecimal price, List<Variant> variants);

    Product publishProduct(User requestingUser, String productId);

    Product suspendProduct(User requestingUser, String productId);

    Product discontinueProduct(User requestingUser, String productId);

    List<Product> consultSellerProducts(User requestingUser, String sellerId);

    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);

    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                       int quantity);

    List<Inventory> consultProductInventory(User requestingUser, String productId);

    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);

    Order consultOrder(User requestingUser, String orderId);

    SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId,
                                                           ReportPeriod period);
}
