package nexusmarket.domain.services.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.ConfirmOrderUseCase;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.inventory.ReserveInventoryService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Converts the requesting buyer's cart into an order in
 * {@code PENDING_PAYMENT}, reserves the inventory of its physical items and
 * empties the cart (order-services.md - ConfirmOrder).
 *
 * <p>Each physical item is reserved in the first warehouse whose record is
 * {@code AVAILABLE} and has enough stock for the whole item. The availability of
 * every item is checked before any reservation is applied.</p>
 */
@Service
@RequiredArgsConstructor
public class ConfirmOrderService implements ConfirmOrderUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ReserveInventoryService reserveInventoryService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Order confirmOrder(User requestingUser) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Buyer buyer = buyerRepositoryPort.findById(requester.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer", requester.getId()));
        if (!buyer.canPurchase()) {
            throw new InvalidUserException("The commercial status " + buyer.getCommercialStatus().getCode()
                    + " of the buyer does not allow purchases.");
        }
        Cart cart = cartRepositoryPort.findByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Cart", "buyer " + buyer.getId()));
        if (cart.isEmpty()) {
            throw new InvalidCartException("The cart is empty.");
        }

        Map<Inventory, Integer> reservations = new LinkedHashMap<>();
        List<OrderItem> items = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = loadPublishedProduct(cartItem.getProduct().getId());
            Warehouse originWarehouse = null;
            if (product instanceof PhysicalProduct physicalProduct) {
                Inventory inventory = allocate(physicalProduct, cartItem.getQuantity(), reservations);
                reservations.merge(inventory, cartItem.getQuantity(), Integer::sum);
                originWarehouse = inventory.getWarehouse();
            }
            items.add(new OrderItem(product, cartItem.getVariant(), cartItem.getQuantity(), cartItem.getUnitPrice(),
                    originWarehouse));
        }
        for (Map.Entry<Inventory, Integer> reservation : reservations.entrySet()) {
            reserveInventoryService.execute(reservation.getKey(), reservation.getValue(), buyer);
        }

        Order order = new Order(UUID.randomUUID().toString(), buyer, items, LocalDateTime.now());
        Order saved = orderRepositoryPort.save(order);
        cart.clear();
        cartRepositoryPort.update(cart);
        registerAuditLogService.execute(buyer, AuditAction.ORDER_CONFIRMED, AuditEntityType.ORDER, saved.getId(),
                Map.of("items", String.valueOf(items.size()), "total", saved.totalAmount().toPlainString()));
        return saved;
    }

    private Product loadPublishedProduct(String productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!product.isPublished()) {
            throw new InvalidProductStatusException("Product " + productId + " is no longer published.");
        }
        return product;
    }

    /** First AVAILABLE record with enough stock for the whole item, considering reservations already planned. */
    private Inventory allocate(PhysicalProduct product, int quantity, Map<Inventory, Integer> planned) {
        int bestAvailable = 0;
        for (Inventory inventory : inventoryRepositoryPort.findByProduct(product)) {
            int alreadyPlanned = planned.getOrDefault(inventory, 0);
            if (inventory.canReserve(alreadyPlanned + quantity)) {
                return inventory;
            }
            bestAvailable = Math.max(bestAvailable, inventory.getQuantity() - alreadyPlanned);
        }
        throw new InsufficientInventoryException(quantity, bestAvailable);
    }
}
