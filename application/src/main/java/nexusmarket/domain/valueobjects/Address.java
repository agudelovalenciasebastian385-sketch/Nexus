package nexusmarket.domain.valueobjects;

import java.util.Objects;

import nexusmarket.domain.exceptions.InvalidAddressException;

/**
 * Address (value object).
 *
 * <p>Represents a physical location used for order delivery or warehouse
 * siting. It has no identity of its own: two addresses with the same values
 * are considered equivalent.</p>
 *
 * <p>Immutable and compared by value.</p>
 */
public final class Address {

    private final String line1;
    /** Complementary information. Optional; {@code null} when not provided. */
    private final String line2;
    private final String city;
    private final String state;
    private final String country;

    public Address(String line1, String line2, String city, String state, String country) {
        this.line1 = requireText(line1, "Address line1 cannot be empty.");
        this.line2 = line2 == null || line2.isBlank() ? null : line2;
        this.city = requireText(city, "Address city cannot be empty.");
        this.state = requireText(state, "Address state cannot be empty.");
        this.country = requireText(country, "Address country cannot be empty.");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidAddressException(message);
        }
        return value;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        Address otherAddress = (Address) other;
        return line1.equals(otherAddress.line1)
                && Objects.equals(line2, otherAddress.line2)
                && city.equals(otherAddress.city)
                && state.equals(otherAddress.state)
                && country.equals(otherAddress.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, city, state, country);
    }

    @Override
    public String toString() {
        return line1 + (line2 == null ? "" : ", " + line2) + ", " + city + ", " + state + ", " + country;
    }
}
