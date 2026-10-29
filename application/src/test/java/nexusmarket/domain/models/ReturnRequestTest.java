package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidReturnRequestException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ReturnStatus;
import org.junit.jupiter.api.Test;

class ReturnRequestTest {

    private final Order order = physicalOrder(OrderStatus.DELIVERED);

    private ReturnRequest returnRequest(ReturnStatus status) {
        return new ReturnRequest("return-1", order, "Wrong color", status);
    }

    @Test
    void newReturnRequestStartsRequested() {
        assertThat(new ReturnRequest("return-1", order, "Wrong color").getStatus()).isEqualTo(ReturnStatus.REQUESTED);
    }

    @Test
    void requestedReturnCanBeApproved() {
        ReturnRequest returnRequest = returnRequest(ReturnStatus.REQUESTED);

        returnRequest.approve();

        assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.APPROVED);
    }

    @Test
    void requestedReturnCanBeRejected() {
        ReturnRequest returnRequest = returnRequest(ReturnStatus.REQUESTED);

        returnRequest.reject();

        assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.REJECTED);
    }

    @Test
    void approvingTwiceIsRejected() {
        ReturnRequest returnRequest = returnRequest(ReturnStatus.APPROVED);

        assertThatThrownBy(returnRequest::approve).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void rejectingAnApprovedReturnIsRejected() {
        ReturnRequest returnRequest = returnRequest(ReturnStatus.APPROVED);

        assertThatThrownBy(returnRequest::reject).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void blankReasonIsRejected() {
        assertThatThrownBy(() -> new ReturnRequest("return-1", order, " "))
                .isInstanceOf(InvalidReturnRequestException.class);
    }
}
