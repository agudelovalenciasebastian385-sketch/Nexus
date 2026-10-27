package nexusmarket.domain.services.order;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidOrderStatusTransitionException;
import nexusmarket.domain.exceptions.OrderAlreadyFinalizedException;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.PaymentGatewayPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.invoice.GenerateInvoiceService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.PaymentResult;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Processes the payment of the buyer's own {@code PENDING_PAYMENT} order
 * through {@link PaymentGatewayPort} (order-services.md - ValidatePayment).
 *
 * <p>Approved: the order goes to {@code PAID}, its invoice is generated and, if
 * it has no physical items, it is delivered immediately. Rejected: the
 * rejection is audited, {@link PaymentRejectedException} is raised and the order
 * remains in {@code PENDING_PAYMENT}.</p>
 */
@Service
@RequiredArgsConstructor
public class ValidatePaymentService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final GenerateInvoiceService generateInvoiceService;
    private final RegisterAuditLogService registerAuditLogService;

    public Order validatePayment(User requestingUser, String orderId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        validateBuyerOwnershipService.execute(requester, order);
        if (order.isFinalized()) {
            throw new OrderAlreadyFinalizedException(orderId);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusTransitionException(order.getStatus().getCode(), OrderStatus.PAID.getCode());
        }

        PaymentResult result = paymentGatewayPort.processPayment(order);
        if (result != PaymentResult.APPROVED) {
            registerAuditLogService.execute(requester, AuditAction.PAYMENT_REJECTED, AuditEntityType.ORDER, orderId,
                    Map.of("amount", order.totalAmount().toPlainString()));
            throw new PaymentRejectedException(orderId);
        }

        order.markPaid();
        generateInvoiceService.generateInvoice(order, requester);
        registerAuditLogService.execute(requester, AuditAction.PAYMENT_APPROVED, AuditEntityType.ORDER, orderId,
                Map.of("amount", order.totalAmount().toPlainString()));
        if (!order.hasPhysicalItems()) {
            order.markDelivered();
            registerAuditLogService.execute(requester, AuditAction.ORDER_DELIVERED, AuditEntityType.ORDER, orderId,
                    Map.of("reason", "digital products delivered after payment"));
        }
        orderRepositoryPort.update(order);
        return order;
    }
}
