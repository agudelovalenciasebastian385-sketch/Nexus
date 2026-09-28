package nexusmarket.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Input port: RegisterProduct use case.
 *
 * <p>Registers a physical or digital product of the requesting seller in {@code DRAFT} status.</p>
 */
public interface RegisterProductUseCase {

    Product registerProduct(User requestingUser, ProductType type, String name, String description,
                            BigDecimal price, List<Variant> variants);
}
