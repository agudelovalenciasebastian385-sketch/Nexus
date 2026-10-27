package nexusmarket.domain.services.shipment;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Dispatches a packed shipment ({@code PACKED} → {@code IN_TRANSIT}), converts
 * the reservations of its items into {@code SALE_OUTBOUND} movements and moves
 * the order to {@code SHIPPED} when its first shipment is dispatched
 * (shipment-services.md - DispatchShipment).
 */
@Service
@RequiredArgsConstructor
public class DispatchShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Shipment dispatchShipment(User requestingUser, String shipmentId) {
        User operator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Shipment shipment = shipmentRepositoryPort.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment", shipmentId));
        if (!shipment.getLogisticsOperator().getId().equals(operator.getId())) {
            throw new UnauthorizedOperationException("Shipment " + shipmentId
                    + " is assigned to another logistics operator (RG-03).");
        }
        String orderId = shipment.getOrder().getId();
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        Map<Inventory, Integer> saleOutbounds = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.isPhysical() && item.getOriginWarehouse().getId().equals(shipment.getOriginWarehouse().getId())) {
                PhysicalProduct product = (PhysicalProduct) item.getProduct();
                Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product,
                                shipment.getOriginWarehouse())
                        .orElseThrow(() -> new EntityNotFoundException("Inventory",
                                product.getId() + ":" + shipment.getOriginWarehouse().getId()));
                saleOutbounds.merge(inventory, item.getQuantity(), Integer::sum);
            }
        }

        shipment.markInTransit();
        if (order.getStatus() == OrderStatus.PAID) {
            order.markShipped();
        }
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Inventory, Integer> saleOutbound : saleOutbounds.entrySet()) {
            saleOutbound.getKey().registerMovement(MovementType.SALE_OUTBOUND, saleOutbound.getValue(), now);
        }
        for (Inventory inventory : saleOutbounds.keySet()) {
            inventoryRepositoryPort.update(inventory);
        }
        shipmentRepositoryPort.update(shipment);
        orderRepositoryPort.update(order);
        registerAuditLogService.execute(operator, AuditAction.SHIPMENT_DISPATCHED, AuditEntityType.SHIPMENT,
                shipmentId, Map.of("order", orderId, "orderStatus", order.getStatus().getCode()));
        return shipment;
    }
}
