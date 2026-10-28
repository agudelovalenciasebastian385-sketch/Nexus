package nexusmarket.adapters.persistence.jpa;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.UserJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Seller backed by Spring Data JPA (MySQL).
 *
 * <p>Sellers live in the users table (role SELLER). Their warehouses and products are read
 * through WarehouseRepositoryPort and ProductRepositoryPort.
 */
@Repository
public class SellerJpaAdapter implements SellerRepositoryPort {

    private final SpringDataJpaUserRepository userRepository;

    public SellerJpaAdapter(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Seller save(Seller seller) {
        userRepository.save(UserJpaMapper.toEntity(seller));
        return seller;
    }

    @Override
    public void update(Seller seller) {
        userRepository.save(UserJpaMapper.toEntity(seller));
    }

    @Override
    public Optional<Seller> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findByIdAndRole(id, SystemRole.SELLER.getCode())
                .map(entity -> (Seller) UserJpaMapper.toDomain(entity));
    }
}
