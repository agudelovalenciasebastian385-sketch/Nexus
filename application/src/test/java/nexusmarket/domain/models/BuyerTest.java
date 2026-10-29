package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.PASSWORD_HASH;
import static nexusmarket.domain.DomainFixtures.buyer;
import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BuyerTest {

    private static final Address OTHER = new Address("Carrera 45 # 30-12", null, "Bogota", "Cundinamarca", "Colombia");

    @Test
    void primaryAddressIsRequired() {
        assertThatThrownBy(() -> new Buyer("buyer-1", "1020", "Maria", "maria@gmail.com", PASSWORD_HASH,
                UserStatus.ACTIVE, null, CommercialStatus.ENABLED)).isInstanceOf(InvalidUserException.class);
    }

    @Test
    void additionalAddressIsAdded() {
        Buyer buyer = buyer();

        buyer.addAdditionalAddress(OTHER);

        assertThat(buyer.getAdditionalAddresses()).containsExactly(OTHER);
    }

    @Test
    void duplicateAdditionalAddressIsRejected() {
        Buyer buyer = buyer();
        buyer.addAdditionalAddress(OTHER);

        assertThatThrownBy(() -> buyer.addAdditionalAddress(OTHER)).isInstanceOf(InvalidUserException.class);
    }

    @Test
    void removingAnUnknownAddressIsRejected() {
        assertThatThrownBy(() -> buyer().removeAdditionalAddress(OTHER)).isInstanceOf(InvalidUserException.class);
    }

    @Test
    void activeAndEnabledBuyerCanPurchase() {
        assertThat(buyer().canPurchase()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CommercialStatus.class, names = {"RESTRICTED", "SUSPENDED"})
    void buyerWhoseCommercialStatusIsNotEnabledCannotPurchase(CommercialStatus status) {
        assertThat(buyer(UserStatus.ACTIVE, status).canPurchase()).isFalse();
    }

    @Test
    void blockedBuyerCannotPurchase() {
        assertThat(buyer(UserStatus.BLOCKED, CommercialStatus.ENABLED).canPurchase()).isFalse();
    }

    @Test
    void ownCartIsAssigned() {
        Buyer buyer = buyer();
        Cart cart = new Cart("cart-1", buyer);

        buyer.assignCart(cart);

        assertThat(buyer.getCart()).isSameAs(cart);
    }

    @Test
    void cartOfAnotherBuyerIsRejected() {
        Buyer buyer = buyer();

        assertThatThrownBy(() -> buyer.assignCart(new Cart("cart-1", buyer())))
                .isInstanceOf(InvalidCartException.class);
    }

    @Test
    void secondCartIsRejected() {
        Buyer buyer = buyer();
        buyer.assignCart(new Cart("cart-1", buyer));

        assertThatThrownBy(() -> buyer.assignCart(new Cart("cart-2", buyer))).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void orderOfAnotherBuyerIsRejected() {
        Buyer buyer = buyer();

        assertThatThrownBy(() -> buyer.addOrder(physicalOrder(buyer()))).isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void primaryAddressCanBeReplaced() {
        Buyer buyer = buyer();

        buyer.setPrimaryAddress(OTHER);

        assertThat(buyer.getPrimaryAddress()).isEqualTo(OTHER).isNotEqualTo(ADDRESS);
    }
}
