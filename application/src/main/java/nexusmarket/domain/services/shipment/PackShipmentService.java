package nexusmarket.domain.services.shipment;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.PackShipmentUseCase;
import nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Registers that a shipment has been packed ({@code PREPARING} →
 * {@code PACKED}) (shipment-services.md - PackShipment). Only the operator
 * assigned to the shipment can operate it.
 */
@Service
@RequiredArgsConstructor
public class PackShipmentService implements PackShipmentUseCase {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Shipment packShipment(User requestingUser, String shipmentId) {
        User operator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Shipment shipment = shipmentRepositoryPort.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment", shipmentId));
        if (!shipment.getLogisticsOperator().getId().equals(operator.getId())) {
            throw new UnauthorizedOperationException("Shipment " + shipmentId
                    + " is assigned to another logistics operator (RG-03).");
        }

        shipment.markPacked();
        shipmentRepositoryPort.update(shipment);
        registerAuditLogService.execute(operator, AuditAction.SHIPMENT_PACKED, AuditEntityType.SHIPMENT, shipmentId,
                Map.of("order", shipment.getOrder().getId()));
        return shipment;
    }
}
