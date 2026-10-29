package nexusmarket.domain.services.order;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.digitalItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidOrderStatusTransitionException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.OrderAlreadyFinalizedException;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Buyer buyer = h.buyer();

    OrderServicesTest() {
        h.stock(mouse, warehouse, 10);
    }

    @Nested
    class ConfirmOrder {

        @Test
        void confirmingCreatesAPendingOrderWithTheCartItems() {
            Order order = h.confirmedOrder(buyer, mouse, 2);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(h.orders.findById(order.getId())).isPresent();
        }

        @Test
        void confirmingReservesTheStock() {
            h.confirmedOrder(buyer, mouse, 2);

            assertThat(h.inventoryOf(mouse, warehouse).getQuantity()).isEqualTo(8);
            assertThat(h.inventoryOf(mouse, warehouse).getReservedQuantity()).isEqualTo(2);
        }

        @Test
        void confirmingEmptiesTheCart() {
            h.confirmedOrder(buyer, mouse, 2);

            assertThat(h.carts.findByBuyer(buyer).orElseThrow().isEmpty()).isTrue();
        }

        @Test
        void confirmingAuditsTheReservationAndTheOrder() {
            h.confirmedOrder(buyer, mouse, 2);

            assertThat(h.audit.actions()).containsSubsequence(AuditAction.INVENTORY_RESERVED, AuditAction.ORDER_CONFIRMED);
        }

        @Test
        void stockIsTakenFromAWarehouseWithEnoughUnits() {
            PhysicalProduct lamp = h.physicalProduct(seller, "50.00");
            Warehouse second = h.warehouseOf(seller);
            h.stock(lamp, warehouse, 1);
            h.stock(lamp, second, 5);

            Order order = h.confirmedOrder(buyer, lamp, 3);

            assertThat(order.getItems().get(0).getOriginWarehouse()).isEqualTo(second);
        }

        @Test
        void digitalProductsDoNotReserveStock() {
            DigitalProduct ebook = h.digitalProduct(seller, "30.00");

            Order order = h.confirmedOrder(buyer, ebook, 1);

            assertThat(order.getItems().get(0).getOriginWarehouse()).isNull();
            assertThat(h.audit.actions()).doesNotContain(AuditAction.INVENTORY_RESERVED);
        }

        @Test
        void emptyCartIsRejected() {
            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer)).isInstanceOf(InvalidCartException.class);
        }

        @Test
        void buyerWhoseCommercialStatusIsNotEnabledCannotConfirm() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 1);
            buyer.setCommercialStatus(CommercialStatus.RESTRICTED);

            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer)).isInstanceOf(InvalidUserException.class);
        }

        @Test
        void insufficientStockIsRejected() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 11);

            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer))
                    .isInstanceOf(InsufficientInventoryException.class);
        }

        @Test
        void rejectedOrderReservesNothing() {
            PhysicalProduct lamp = h.physicalProduct(seller, "50.00");
            h.stock(lamp, warehouse, 1);
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);
            h.addItemToCart.addItemToCart(buyer, lamp.getId(), null, 2);

            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer))
                    .isInstanceOf(InsufficientInventoryException.class);
            assertThat(h.inventoryOf(mouse, warehouse).getReservedQuantity()).isZero();
        }

        @Test
        void productNoLongerPublishedIsRejected() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 1);
            mouse.suspend();

            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        void sellerCannotConfirmAnOrder() {
            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(seller))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void blockedBuyerCannotConfirmAnOrder() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 1);
            h.blocked(buyer);

            assertThatThrownBy(() -> h.confirmOrder.confirmOrder(buyer))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class ValidatePayment {

        @Test
        void approvedPaymentMarksTheOrderPaid() {
            Order order = h.paidOrder(buyer, mouse, 2);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        void approvedPaymentGeneratesTheInvoiceForTheOrderTotal() {
            Order order = h.paidOrder(buyer, mouse, 2);

            assertThat(h.invoices.findByOrder(order).orElseThrow().getTotalAmount())
                    .isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        void approvedPaymentIsAudited() {
            h.paidOrder(buyer, mouse, 2);

            assertThat(h.audit.actions()).containsSubsequence(AuditAction.INVOICE_GENERATED,
                    AuditAction.PAYMENT_APPROVED);
        }

        @Test
        void paidDigitalOnlyOrderIsDelivered() {
            Order order = h.paidOrder(buyer, h.digitalProduct(seller, "30.00"), 1);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(h.audit.actions()).contains(AuditAction.ORDER_DELIVERED);
        }

        @Test
        void rejectedPaymentKeepsTheOrderPendingWithoutInvoice() {
            Order order = h.confirmedOrder(buyer, mouse, 2);
            h.payments.rejectNextPayments();

            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, order.getId()))
                    .isInstanceOf(PaymentRejectedException.class);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(h.invoices.existsByOrder(order)).isFalse();
        }

        @Test
        void rejectedPaymentIsAudited() {
            Order order = h.confirmedOrder(buyer, mouse, 2);
            h.payments.rejectNextPayments();

            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, order.getId()))
                    .isInstanceOf(PaymentRejectedException.class);
            assertThat(h.audit.actions()).contains(AuditAction.PAYMENT_REJECTED);
        }

        @Test
        void payingAnAlreadyPaidOrderIsRejected() {
            Order order = h.paidOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, order.getId()))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        void payingADeliveredOrderIsRejected() {
            Order order = h.paidOrder(buyer, h.digitalProduct(seller, "30.00"), 1);

            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, order.getId()))
                    .isInstanceOf(OrderAlreadyFinalizedException.class);
        }

        @Test
        void payingAnotherBuyersOrderIsRejected() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.validatePayment.validatePayment(h.buyer(), order.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void payingAnUnknownOrderIsRejected() {
            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void gatewayIsNotCalledForAnOrderThatCannotBePaid() {
            Order order = h.paidOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.validatePayment.validatePayment(buyer, order.getId()))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
            assertThat(h.payments.processedOrders()).containsExactly(order.getId());
        }
    }

    @Nested
    class ConsultOrder {

        @Test
        void buyerConsultsItsOwnOrder() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThat(h.consultOrder.consultOrder(buyer, order.getId())).isSameAs(order);
        }

        @Test
        void buyerCannotConsultAnotherBuyersOrder() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.consultOrder.consultOrder(h.buyer(), order.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void sellerWithProductsInTheOrderCanConsultIt() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThat(h.consultOrder.consultOrder(seller, order.getId())).isSameAs(order);
        }

        @Test
        void sellerWithoutProductsInTheOrderCannotConsultIt() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.consultOrder.consultOrder(h.seller(), order.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void supervisorConsultsAnyOrder() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThat(h.consultOrder.consultOrder(h.supervisor(), order.getId())).isSameAs(order);
        }

        @Test
        void administratorIsNotAllowedToConsultOrders() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThatThrownBy(() -> h.consultOrder.consultOrder(h.admin(), order.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void unknownOrderIsReported() {
            assertThatThrownBy(() -> h.consultOrder.consultOrder(buyer, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ConsultBuyerOrders {

        @Test
        void buyerConsultsItsOwnOrders() {
            Order order = h.confirmedOrder(buyer, mouse, 1);

            assertThat(h.consultBuyerOrders.consultBuyerOrders(buyer, buyer.getId())).containsExactly(order);
        }

        @Test
        void buyerCannotConsultAnotherBuyersOrders() {
            assertThatThrownBy(() -> h.consultBuyerOrders.consultBuyerOrders(h.buyer(), buyer.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void supervisorConsultsTheOrdersOfAnyBuyer() {
            h.orders.save(new Order("order-x", buyer, List.of(digitalItem(h.digitalProduct(seller, "10.00"), 1)), NOW));

            assertThat(h.consultBuyerOrders.consultBuyerOrders(h.supervisor(), buyer.getId())).hasSize(1);
        }

        @Test
        void unknownBuyerIsReported() {
            assertThatThrownBy(() -> h.consultBuyerOrders.consultBuyerOrders(h.supervisor(), "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
