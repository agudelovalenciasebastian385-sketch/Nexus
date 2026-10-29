package nexusmarket.domain.services.returns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidReturnRequestException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.RefundStatus;
import nexusmarket.domain.valueobjects.ReturnStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReturnsServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Buyer buyer = h.buyer();
    private final Administrator admin = h.admin();

    ReturnsServicesTest() {
        h.stock(mouse, warehouse, 10);
    }

    private Order deliveredOrder() {
        return h.deliveredOrder(buyer, mouse, 2, h.operator());
    }

    private ReturnRequest requestedReturn() {
        return h.requestReturn.requestReturn(buyer, deliveredOrder().getId(), "Wrong color");
    }

    @Nested
    class RequestReturn {

        @Test
        void buyerRequestsTheReturnOfADeliveredOrder() {
            ReturnRequest returnRequest = requestedReturn();

            assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.REQUESTED);
            assertThat(h.returnRequests.findById(returnRequest.getId())).isPresent();
            assertThat(h.audit.actions()).contains(AuditAction.RETURN_REQUESTED);
        }

        @Test
        void orderThatIsNotDeliveredCannotBeReturned() {
            Order paid = h.paidOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.requestReturn.requestReturn(buyer, paid.getId(), "Changed my mind"))
                    .isInstanceOf(InvalidReturnRequestException.class);
        }

        @Test
        void orderWithAnActiveReturnCannotBeReturnedAgain() {
            Order order = deliveredOrder();
            h.requestReturn.requestReturn(buyer, order.getId(), "Wrong color");

            assertThatThrownBy(() -> h.requestReturn.requestReturn(buyer, order.getId(), "Again"))
                    .isInstanceOf(InvalidReturnRequestException.class);
        }

        @Test
        void orderWhoseReturnWasRejectedCanBeReturnedAgain() {
            Order order = deliveredOrder();
            ReturnRequest first = h.requestReturn.requestReturn(buyer, order.getId(), "Wrong color");
            h.rejectReturn.rejectReturn(admin, first.getId());

            ReturnRequest second = h.requestReturn.requestReturn(buyer, order.getId(), "Damaged");

            assertThat(second.getStatus()).isEqualTo(ReturnStatus.REQUESTED);
        }

        @Test
        void buyerCannotReturnAnotherBuyersOrder() {
            Order order = deliveredOrder();

            assertThatThrownBy(() -> h.requestReturn.requestReturn(h.buyer(), order.getId(), "Not mine"))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void unknownOrderIsReported() {
            assertThatThrownBy(() -> h.requestReturn.requestReturn(buyer, "missing", "Reason"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ApproveAndReject {

        @Test
        void approvingMarksTheReturnApproved() {
            ReturnRequest returnRequest = requestedReturn();

            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.APPROVED);
        }

        @Test
        void approvingPutsTheReturnedUnitsBackInStock() {
            ReturnRequest returnRequest = requestedReturn();

            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThat(h.inventoryOf(mouse, warehouse).getQuantity()).isEqualTo(10);
            assertThat(h.inventoryOf(mouse, warehouse).getMovements()).extracting(InventoryMovement::getType)
                    .endsWith(MovementType.RETURN);
        }

        @Test
        void approvingCreatesAPendingRefundForTheInvoiceTotal() {
            ReturnRequest returnRequest = requestedReturn();

            h.approveReturn.approveReturn(admin, returnRequest.getId());

            Refund refund = h.refunds.findByReturnRequest(returnRequest).orElseThrow();
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
            assertThat(refund.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        void approvingIsAudited() {
            ReturnRequest returnRequest = requestedReturn();

            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThat(h.audit.actions()).containsSubsequence(AuditAction.RETURN_APPROVED, AuditAction.REFUND_CREATED);
        }

        @Test
        void approvingTwiceIsRejected() {
            ReturnRequest returnRequest = requestedReturn();
            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThatThrownBy(() -> h.approveReturn.approveReturn(admin, returnRequest.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        void onlyAnAdministratorApprovesReturns() {
            ReturnRequest returnRequest = requestedReturn();

            assertThatThrownBy(() -> h.approveReturn.approveReturn(h.supervisor(), returnRequest.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void approvingAnUnknownReturnIsReported() {
            assertThatThrownBy(() -> h.approveReturn.approveReturn(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void rejectingMarksTheReturnRejectedAndIsAudited() {
            ReturnRequest returnRequest = requestedReturn();

            h.rejectReturn.rejectReturn(admin, returnRequest.getId());

            assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.REJECTED);
            assertThat(h.audit.actions()).contains(AuditAction.RETURN_REJECTED);
        }

        @Test
        void rejectingAnApprovedReturnIsRejected() {
            ReturnRequest returnRequest = requestedReturn();
            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThatThrownBy(() -> h.rejectReturn.rejectReturn(admin, returnRequest.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        void rejectingAnUnknownReturnIsReported() {
            assertThatThrownBy(() -> h.rejectReturn.rejectReturn(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ProcessRefund {

        private Refund pendingRefund() {
            ReturnRequest returnRequest = requestedReturn();
            h.approveReturn.approveReturn(admin, returnRequest.getId());
            return h.refunds.findByReturnRequest(returnRequest).orElseThrow();
        }

        @Test
        void processingMarksTheRefundProcessedWithItsDate() {
            Refund refund = h.processRefund.processRefund(admin, pendingRefund().getId());

            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PROCESSED);
            assertThat(refund.getProcessedAt()).isNotNull();
            assertThat(h.audit.actions()).contains(AuditAction.REFUND_PROCESSED);
        }

        @Test
        void processingTwiceIsRejected() {
            Refund refund = pendingRefund();
            h.processRefund.processRefund(admin, refund.getId());

            assertThatThrownBy(() -> h.processRefund.processRefund(admin, refund.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        void onlyAnAdministratorProcessesRefunds() {
            Refund refund = pendingRefund();

            assertThatThrownBy(() -> h.processRefund.processRefund(buyer, refund.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void processingAnUnknownRefundIsReported() {
            assertThatThrownBy(() -> h.processRefund.processRefund(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ConsultReturn {

        @Test
        void buyerConsultsItsOwnReturn() {
            ReturnRequest returnRequest = requestedReturn();

            assertThat(h.consultReturn.consultReturn(buyer, returnRequest.getId())).isSameAs(returnRequest);
        }

        @Test
        void buyerCannotConsultAnotherBuyersReturn() {
            ReturnRequest returnRequest = requestedReturn();

            assertThatThrownBy(() -> h.consultReturn.consultReturn(h.buyer(), returnRequest.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void administratorConsultsTheRefundOfAReturn() {
            ReturnRequest returnRequest = requestedReturn();
            h.approveReturn.approveReturn(admin, returnRequest.getId());

            assertThat(h.consultReturn.consultRefund(admin, returnRequest.getId()).getReturnRequest())
                    .isSameAs(returnRequest);
        }

        @Test
        void refundOfAReturnNotYetApprovedIsReported() {
            ReturnRequest returnRequest = requestedReturn();

            assertThatThrownBy(() -> h.consultReturn.consultRefund(admin, returnRequest.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void sellerIsNotAllowedToConsultReturns() {
            ReturnRequest returnRequest = requestedReturn();

            assertThatThrownBy(() -> h.consultReturn.consultReturn(seller, returnRequest.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void unknownReturnIsReported() {
            assertThatThrownBy(() -> h.consultReturn.consultReturn(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
