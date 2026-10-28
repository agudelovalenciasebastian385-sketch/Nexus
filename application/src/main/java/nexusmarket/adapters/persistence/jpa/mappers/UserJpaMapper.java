package nexusmarket.adapters.persistence.jpa.mappers;

import java.util.ArrayList;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.AddressEmbeddable;
import nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Bidirectional mapper between the User hierarchy and the single users table.
 *
 * <p>The role column selects the subclass to rebuild. Every subclass is rebuilt through its
 * rehydration constructor, including authTokenVersion. A Buyer is rebuilt with its addresses and
 * commercial status only: its cart and orders are separate aggregates loaded through
 * CartRepositoryPort and OrderRepositoryPort.
 */
public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setIdentificationNumber(domain.getIdentificationNumber());
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole().getCode());
        entity.setStatus(domain.getStatus().getCode());
        entity.setAuthTokenVersion(domain.getAuthTokenVersion());
        if (domain instanceof Buyer buyer) {
            entity.setCommercialStatus(buyer.getCommercialStatus().getCode());
            entity.setPrimaryAddress(AddressJpaMapper.toEmbeddable(buyer.getPrimaryAddress()));
            List<AddressEmbeddable> additional = new ArrayList<>();
            for (Address address : buyer.getAdditionalAddresses()) {
                additional.add(AddressJpaMapper.toEmbeddable(address));
            }
            entity.setAdditionalAddresses(additional);
        }
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        UserStatus status = UserStatus.fromCode(entity.getStatus());
        Integer version = entity.getAuthTokenVersion() != null ? entity.getAuthTokenVersion() : 1;
        return switch (SystemRole.fromCode(entity.getRole())) {
            case ADMINISTRATOR -> new Administrator(entity.getId(), entity.getIdentificationNumber(),
                    entity.getFullName(), entity.getEmail(), entity.getPassword(), status, version);
            case SUPERVISOR -> new Supervisor(entity.getId(), entity.getIdentificationNumber(),
                    entity.getFullName(), entity.getEmail(), entity.getPassword(), status, version);
            case LOGISTICS_OPERATOR -> new LogisticsOperator(entity.getId(), entity.getIdentificationNumber(),
                    entity.getFullName(), entity.getEmail(), entity.getPassword(), status, version);
            case SELLER -> new Seller(entity.getId(), entity.getIdentificationNumber(),
                    entity.getFullName(), entity.getEmail(), entity.getPassword(), status, version);
            case BUYER -> toBuyer(entity, status, version);
        };
    }

    private static Buyer toBuyer(UserJpaEntity entity, UserStatus status, Integer version) {
        List<Address> additional = new ArrayList<>();
        if (entity.getAdditionalAddresses() != null) {
            for (AddressEmbeddable embeddable : entity.getAdditionalAddresses()) {
                additional.add(AddressJpaMapper.toDomain(embeddable));
            }
        }
        return new Buyer(entity.getId(), entity.getIdentificationNumber(), entity.getFullName(),
                entity.getEmail(), entity.getPassword(), status,
                AddressJpaMapper.toDomain(entity.getPrimaryAddress()), additional,
                CommercialStatus.fromCode(entity.getCommercialStatus()), null, version);
    }
}
