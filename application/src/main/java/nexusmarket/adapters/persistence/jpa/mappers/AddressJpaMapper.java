package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.AddressEmbeddable;
import nexusmarket.domain.valueobjects.Address;

/**
 * Bidirectional mapper between the Address value object and its embeddable.
 *
 * <p>An embeddable whose columns are all null (for example the primary address of a user
 * who is not a Buyer) maps to a null Address.
 */
public final class AddressJpaMapper {

    private AddressJpaMapper() {
    }

    public static AddressEmbeddable toEmbeddable(Address domain) {
        if (domain == null) {
            return null;
        }
        AddressEmbeddable embeddable = new AddressEmbeddable();
        embeddable.setLine1(domain.getLine1());
        embeddable.setLine2(domain.getLine2());
        embeddable.setCity(domain.getCity());
        embeddable.setState(domain.getState());
        embeddable.setCountry(domain.getCountry());
        return embeddable;
    }

    public static Address toDomain(AddressEmbeddable embeddable) {
        if (embeddable == null || embeddable.getLine1() == null) {
            return null;
        }
        return new Address(embeddable.getLine1(), embeddable.getLine2(), embeddable.getCity(),
                embeddable.getState(), embeddable.getCountry());
    }
}
