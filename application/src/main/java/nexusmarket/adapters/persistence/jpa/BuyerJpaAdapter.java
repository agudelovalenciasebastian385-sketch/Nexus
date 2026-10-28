package nexusmarket.adapters.persistence.jpa;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.UserJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Buyer backed by Spring Data JPA (MySQL).
 *
 * <p>Buyers live in the users table (role BUYER) with their own columns and the
 * buyer_additional_addresses collection table.
 */
@Repository
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private final SpringDataJpaUserRepository userRepository;

    public BuyerJpaAdapter(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Buyer save(Buyer buyer) {
        userRepository.save(UserJpaMapper.toEntity(buyer));
        return buyer;
    }

    @Override
    public void update(Buyer buyer) {
        userRepository.save(UserJpaMapper.toEntity(buyer));
    }

    @Override
    public Optional<Buyer> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findByIdAndRole(id, SystemRole.BUYER.getCode())
                .map(entity -> (Buyer) UserJpaMapper.toDomain(entity));
    }
}
