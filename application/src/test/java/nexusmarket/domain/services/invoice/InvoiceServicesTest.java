package nexusmarket.domain.services.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import org.junit.jupiter.api.Test;

class InvoiceServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Buyer buyer = h.buyer();

    InvoiceServicesTest() {
        h.stock(mouse, h.firstWarehouse(seller), 10);
    }

    @Test
    void paidOrderIsInvoicedForItsTotal() {
        Order order = h.paidOrder(buyer, mouse, 3);

        Invoice invoice = h.invoices.findByOrder(order).orElseThrow();

        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(order.totalAmount());
        assertThat(order.getInvoice()).isSameAs(invoice);
    }

    @Test
    void invoiceGenerationIsAudited() {
        h.paidOrder(buyer, mouse, 1);

        assertThat(h.audit.actions()).contains(AuditAction.INVOICE_GENERATED);
    }

    @Test
    void unpaidOrderCannotBeInvoiced() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.generateInvoice.generateInvoice(order, buyer))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void secondInvoiceForTheSameOrderIsRejected() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.generateInvoice.generateInvoice(order, buyer))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void buyerConsultsTheInvoiceOfItsOwnOrder() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThat(h.consultInvoice.consultInvoice(buyer, order.getId()).getOrder()).isSameAs(order);
    }

    @Test
    void buyerCannotConsultTheInvoiceOfAnotherBuyer() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.consultInvoice.consultInvoice(h.buyer(), order.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void administratorConsultsAnyInvoice() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThat(h.consultInvoice.consultInvoice(h.admin(), order.getId())).isNotNull();
    }

    @Test
    void sellerIsNotAllowedToConsultInvoices() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.consultInvoice.consultInvoice(seller, order.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void orderWithoutInvoiceIsReported() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThatThrownBy(() -> h.consultInvoice.consultInvoice(buyer, order.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void unknownOrderIsReported() {
        assertThatThrownBy(() -> h.consultInvoice.consultInvoice(buyer, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
