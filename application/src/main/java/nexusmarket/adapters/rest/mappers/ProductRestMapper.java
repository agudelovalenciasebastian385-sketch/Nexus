package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.VariantResponseDTO;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Mapper from the Product Domain Model (physical or digital) to its REST DTO.
 */
public final class ProductRestMapper {

    private ProductRestMapper() {
    }

    public static ProductResponseDTO toResponse(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setType(product.getType().getCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus().getCode());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getFullName());
        dto.setVariants(product.getVariants().stream().map(ProductRestMapper::toResponse).toList());
        return dto;
    }

    public static List<ProductResponseDTO> toResponse(List<Product> products) {
        return products.stream().map(ProductRestMapper::toResponse).toList();
    }

    public static VariantResponseDTO toResponse(Variant variant) {
        VariantResponseDTO dto = new VariantResponseDTO();
        dto.setType(variant.getType());
        dto.setValue(variant.getValue());
        return dto;
    }
}
