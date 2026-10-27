package nexusmarket.domain.services.shipment;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Confirms the delivery of a shipment ({@code IN_TRANSIT} → {@code DELIVERED})
 * and finalizes the order when all its shipments are delivered
 * (shipment-services.md - ConfirmDelivery).
 */
@Service
@RequiredArgsConstructor
public class ConfirmDeliveryService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Shipment confirmDelivery(User requestingUser, String shipmentId) {
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

        shipment.markDelivered();
        shipmentRepositoryPort.update(shipment);
        registerAuditLogService.execute(operator, AuditAction.SHIPMENT_DELIVERED, AuditEntityType.SHIPMENT,
                shipmentId, Map.of("order", orderId));

        boolean allDelivered = shipmentRepositoryPort.findByOrder(order).stream()
                .allMatch(other -> other.getId().equals(shipmentId) || other.isDelivered());
        if (allDelivered) {
            order.markDelivered();
            orderRepositoryPort.update(order);
            registerAuditLogService.execute(operator, AuditAction.ORDER_DELIVERED, AuditEntityType.ORDER, orderId,
                    Map.of("lastShipment", shipmentId));
        }
        return shipment;
    }
}
