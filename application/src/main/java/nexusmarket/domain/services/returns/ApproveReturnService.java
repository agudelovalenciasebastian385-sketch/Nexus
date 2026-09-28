package nexusmarket.domain.services.returns;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ApproveReturnUseCase;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import nexusmarket.domain.ports.out.RefundRepositoryPort;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Approves a return request, re-enters the inventory of its physical items with
 * {@code RETURN} movements and creates the pending refund for the invoice total
 * (returns-services.md - ApproveReturn). Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class ApproveReturnService implements ApproveReturnUseCase {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public ReturnRequest approveReturn(User requestingUser, String returnRequestId) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        ReturnRequest returnRequest = returnRequestRepositoryPort.findById(returnRequestId)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest", returnRequestId));
        Order order = returnRequest.getOrder();
        Invoice invoice = invoiceRepositoryPort.findByOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Invoice", "order " + order.getId()));
        Map<Inventory, Integer> reentries = loadReentries(order);

        returnRequest.approve();
        Refund refund = new Refund(UUID.randomUUID().toString(), returnRequest, invoice.getTotalAmount());

        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Inventory, Integer> reentry : reentries.entrySet()) {
            reentry.getKey().registerMovement(MovementType.RETURN, reentry.getValue(), now);
            inventoryRepositoryPort.update(reentry.getKey());
        }
        returnRequestRepositoryPort.update(returnRequest);
        Refund savedRefund = refundRepositoryPort.save(refund);
        registerAuditLogService.execute(administrator, AuditAction.RETURN_APPROVED, AuditEntityType.RETURN_REQUEST,
                returnRequestId, Map.of("order", order.getId(), "inventoryRecords", String.valueOf(reentries.size())));
        registerAuditLogService.execute(administrator, AuditAction.REFUND_CREATED, AuditEntityType.REFUND,
                savedRefund.getId(), Map.of("returnRequest", returnRequestId,
                        "amount", savedRefund.getAmount().toPlainString()));
        return returnRequest;
    }

    /** Inventory records of the order's physical items with the quantity to re-enter in each one. */
    private Map<Inventory, Integer> loadReentries(Order order) {
        Map<Inventory, Integer> reentries = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.isPhysical()) {
                PhysicalProduct product = (PhysicalProduct) item.getProduct();
                Inventory inventory = inventoryRepositoryPort.findByProductAndWarehouse(product,
                                item.getOriginWarehouse())
                        .orElseThrow(() -> new EntityNotFoundException("Inventory",
                                product.getId() + ":" + item.getOriginWarehouse().getId()));
                reentries.merge(inventory, item.getQuantity(), Integer::sum);
            }
        }
        return reentries;
    }
}
