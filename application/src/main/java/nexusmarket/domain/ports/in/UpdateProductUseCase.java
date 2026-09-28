package nexusmarket.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Input port: UpdateProduct use case.
 *
 * <p>Updates the commercial information of a product of the requesting seller.</p>
 */
public interface UpdateProductUseCase {

    Product updateProduct(User requestingUser, String productId, String name, String description,
                          BigDecimal price, List<Variant> variants);
}
