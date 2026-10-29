package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.CartItemResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.CartResponseDTO;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;

/**
 * Mapper from the Cart Domain Model to its REST DTO.
 */
public final class CartRestMapper {

    private CartRestMapper() {
    }

    public static CartResponseDTO toResponse(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setBuyerId(cart.getBuyer().getId());
        dto.setItems(cart.getItems().stream().map(CartRestMapper::toResponse).toList());
        dto.setTotalAmount(cart.totalAmount());
        return dto;
    }

    private static CartItemResponseDTO toResponse(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductStatus(item.getProduct().getStatus().getCode());
        dto.setVariant(VariantRestMapper.toResponse(item.getVariant()));
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.subtotal());
        return dto;
    }
}
