package nexusmarket.domain.services.returns;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidReturnRequestException;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReturnStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Registers a return request over a delivered order of the requesting buyer
 * (returns-services.md - RequestReturn). A return covers the whole order, so an
 * order can have at most one return request that is not {@code REJECTED}.
 */
@Service
@RequiredArgsConstructor
public class RequestReturnService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    public ReturnRequest requestReturn(User requestingUser, String orderId, String reason) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        validateBuyerOwnershipService.execute(requester, order);
        if (!order.isFinalized()) {
            throw new InvalidReturnRequestException("Only a delivered order can be returned; order " + orderId
                    + " is " + order.getStatus().getCode() + ".");
        }
        boolean hasActiveReturn = returnRequestRepositoryPort.findByOrder(order).stream()
                .anyMatch(existing -> existing.getStatus() != ReturnStatus.REJECTED);
        if (hasActiveReturn) {
            throw new InvalidReturnRequestException("Order " + orderId + " already has an active return request.");
        }

        ReturnRequest returnRequest = new ReturnRequest(UUID.randomUUID().toString(), order, reason);
        ReturnRequest saved = returnRequestRepositoryPort.save(returnRequest);
        registerAuditLogService.execute(requester, AuditAction.RETURN_REQUESTED, AuditEntityType.RETURN_REQUEST,
                saved.getId(), Map.of("order", orderId));
        return saved;
    }
}
