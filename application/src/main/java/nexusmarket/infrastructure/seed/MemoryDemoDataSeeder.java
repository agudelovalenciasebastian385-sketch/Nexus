package nexusmarket.infrastructure.seed;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.inventory.RegisterInitialStockService;
import nexusmarket.domain.services.product.PublishProductService;
import nexusmarket.domain.services.product.RegisterProductService;
import nexusmarket.domain.services.seller.RegisterSellerService;
import nexusmarket.domain.services.user.RegisterBuyerUserService;
import nexusmarket.domain.services.user.RegisterStaffUserService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sample data of the memory profile, so the system can be tried right after starting it: a seller with a
 * warehouse and published products with stock, a logistics operator, a supervisor and a buyer. It goes
 * through the real services, so the domain rules apply and the passwords are hashed. It runs after
 * DatabaseSeeder and is turned off with app.seed.demo.enabled=false.
 */
@Component
@Profile("memory")
@ConditionalOnProperty(name = "app.seed.demo.enabled", havingValue = "true")
@Order(2)
public class MemoryDemoDataSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryDemoDataSeeder.class);
    private static final Address ADDRESS = new Address("Calle 10 # 20-30", null, "Medellin", "Antioquia", "Colombia");

    static final String SELLER_EMAIL = "vendedor@example.com";
    static final String OPERATOR_EMAIL = "operador@example.com";
    static final String SUPERVISOR_EMAIL = "supervisor@example.com";
    static final String BUYER_EMAIL = "comprador@example.com";

    private final UserRepositoryPort userRepositoryPort;
    private final RegisterSellerService registerSellerService;
    private final RegisterStaffUserService registerStaffUserService;
    private final RegisterBuyerUserService registerBuyerUserService;
    private final RegisterProductService registerProductService;
    private final PublishProductService publishProductService;
    private final RegisterInitialStockService registerInitialStockService;
    private final String administratorEmail;

    public MemoryDemoDataSeeder(UserRepositoryPort userRepositoryPort,
                                RegisterSellerService registerSellerService,
                                RegisterStaffUserService registerStaffUserService,
                                RegisterBuyerUserService registerBuyerUserService,
                                RegisterProductService registerProductService,
                                PublishProductService publishProductService,
                                RegisterInitialStockService registerInitialStockService,
                                @Value("${app.seed.admin.email}") String administratorEmail) {
        this.userRepositoryPort = userRepositoryPort;
        this.registerSellerService = registerSellerService;
        this.registerStaffUserService = registerStaffUserService;
        this.registerBuyerUserService = registerBuyerUserService;
        this.registerProductService = registerProductService;
        this.publishProductService = publishProductService;
        this.registerInitialStockService = registerInitialStockService;
        this.administratorEmail = administratorEmail;
    }

    @Override
    public void run(String... args) {
        if (userRepositoryPort.existsByEmail(SELLER_EMAIL)) {
            return;
        }
        User administrator = userRepositoryPort.findByEmail(administratorEmail).orElseThrow();

        Seller seller = registerSellerService.registerSeller(administrator, "900000001", "Tech Store", SELLER_EMAIL,
                "Seller123*", "Bodega Principal", ADDRESS);
        Warehouse warehouse = seller.getWarehouses().get(0);
        stockedProduct(seller, warehouse, "Wireless Mouse", "Ergonomic wireless mouse", "100.00", 20);
        stockedProduct(seller, warehouse, "Mechanical Keyboard", "Mechanical keyboard with backlight", "250.00", 10);
        publishedProduct(seller, ProductType.DIGITAL, "Java Guide", "Electronic book", "50.00");

        registerStaffUserService.registerStaffUser(administrator, SystemRole.LOGISTICS_OPERATOR, "900000002",
                "Laura Operator", OPERATOR_EMAIL, "Operator123*");
        registerStaffUserService.registerStaffUser(administrator, SystemRole.SUPERVISOR, "900000003",
                "Pedro Supervisor", SUPERVISOR_EMAIL, "Supervisor123*");
        registerBuyerUserService.registerBuyerUser("900000004", "Maria Gomez", BUYER_EMAIL, "Buyer123*", ADDRESS);
        LOGGER.info("MemoryDemoDataSeeder: sample users and products created.");
    }

    private void stockedProduct(Seller seller, Warehouse warehouse, String name, String description, String price,
                                int stock) {
        Product product = publishedProduct(seller, ProductType.PHYSICAL, name, description, price);
        registerInitialStockService.registerInitialStock(seller, product.getId(), warehouse.getId(), stock);
    }

    private Product publishedProduct(Seller seller, ProductType type, String name, String description, String price) {
        Product product = registerProductService.registerProduct(seller, type, name, description,
                new BigDecimal(price), List.of());
        return publishProductService.publishProduct(seller, product.getId());
    }
}
