package nexusmarket.domain.services.shipment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Creates the shipments of a {@code PAID} order: one per origin warehouse,
 * only for physical items (shipment-services.md - CreateShipments). The
 * requesting operator is assigned to every created shipment.
 */
@Service
@RequiredArgsConstructor
public class CreateShipmentsService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public List<Shipment> createShipments(User requestingUser, String orderId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        if (!(requester instanceof LogisticsOperator operator)) {
            throw new EntityNotFoundException("LogisticsOperator", requester.getId());
        }
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidShipmentException("Shipments can only be created for a paid order; order " + orderId
                    + " is " + order.getStatus().getCode() + ".");
        }
        if (!order.hasPhysicalItems()) {
            throw new InvalidShipmentException("Order " + orderId + " has no physical products to ship.");
        }
        if (!shipmentRepositoryPort.findByOrder(order).isEmpty()) {
            throw new InvalidShipmentException("Order " + orderId + " already has shipments.");
        }

        Map<String, Warehouse> originWarehouses = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.isPhysical()) {
                originWarehouses.putIfAbsent(item.getOriginWarehouse().getId(), item.getOriginWarehouse());
            }
        }
        List<Shipment> shipments = new ArrayList<>();
        for (Warehouse warehouse : originWarehouses.values()) {
            Shipment shipment = new Shipment(UUID.randomUUID().toString(), order, warehouse,
                    order.getBuyer().getPrimaryAddress(), operator);
            order.addShipment(shipment);
            shipments.add(shipmentRepositoryPort.save(shipment));
            registerAuditLogService.execute(operator, AuditAction.SHIPMENT_CREATED, AuditEntityType.SHIPMENT,
                    shipment.getId(), Map.of("order", orderId, "originWarehouse", warehouse.getId()));
        }
        orderRepositoryPort.update(order);
        return shipments;
    }
}
