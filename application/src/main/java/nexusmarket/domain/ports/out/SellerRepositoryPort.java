package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.Seller;

/**
 * Output port: Persistence and queries of {@code Seller}.
 */
public interface SellerRepositoryPort {

    Seller save(Seller seller);

    void update(Seller seller);

    Optional<Seller> findById(String id);
}
