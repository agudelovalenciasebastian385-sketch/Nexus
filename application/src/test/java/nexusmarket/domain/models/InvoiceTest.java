package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.InvalidInvoiceException;
import nexusmarket.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    private final Order order = physicalOrder(OrderStatus.PAID);

    @Test
    void totalIsTakenFromTheOrder() {
        assertThat(new Invoice("invoice-1", order, NOW).getTotalAmount()).isEqualByComparingTo(order.totalAmount());
    }

    @Test
    void negativeTotalIsRejected() {
        assertThatThrownBy(() -> new Invoice("invoice-1", order, new BigDecimal("-1"), NOW))
                .isInstanceOf(InvalidInvoiceException.class);
    }

    @Test
    void orderIsRequired() {
        assertThatThrownBy(() -> new Invoice("invoice-1", null, NOW)).isInstanceOf(InvalidInvoiceException.class);
    }

    @Test
    void issuedAtIsRequired() {
        assertThatThrownBy(() -> new Invoice("invoice-1", order, null)).isInstanceOf(InvalidInvoiceException.class);
    }
}
