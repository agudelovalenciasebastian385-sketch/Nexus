package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the User hierarchy.
 *
 * <p>Administrator, Supervisor, LogisticsOperator, Seller and Buyer share the single
 * {@code users} table and are told apart by the {@code role} column (SystemRole code).
 * The Buyer-only data (commercial status, primary address and additional addresses) lives
 * in nullable columns and in the {@code buyer_additional_addresses} collection table; it
 * stays empty for every other role. Seller has no data of its own.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "identification_number", nullable = false, unique = true, length = 40)
    private String identificationNumber;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    /** SystemRole catalog code; discriminates the User subclass. */
    @Column(name = "role", nullable = false, length = 40)
    private String role;

    /** UserStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;

    /**
     * Version bound to issued JWTs ({@code ver} claim). Bumped by the domain on every
     * security-relevant mutation to invalidate previously issued tokens.
     */
    @Column(name = "auth_token_version", nullable = false)
    private Integer authTokenVersion = 1;

    /** Buyer only: CommercialStatus catalog code. */
    @Column(name = "commercial_status", length = 40)
    private String commercialStatus;

    /** Buyer only: Buyer.primaryAddress. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "primary_address_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "primary_address_line2", length = 200)),
            @AttributeOverride(name = "city", column = @Column(name = "primary_address_city", length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "primary_address_state", length = 100)),
            @AttributeOverride(name = "country", column = @Column(name = "primary_address_country", length = 100))
    })
    private AddressEmbeddable primaryAddress;

    /** Buyer only: Buyer.additionalAddresses, in registration order. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "buyer_additional_addresses", joinColumns = @JoinColumn(name = "buyer_id"))
    @OrderColumn(name = "address_order")
    private List<AddressEmbeddable> additionalAddresses = new ArrayList<>();
}
