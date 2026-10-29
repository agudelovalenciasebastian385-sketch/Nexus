package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.OrderItemResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ShipmentResponseDTO;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.Shipment;

/**
 * Mapper from Order (with its items and shipments) and Shipment to REST DTOs.
 */
public final class OrderRestMapper {

    private OrderRestMapper() {
    }

    public static OrderResponseDTO toResponse(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());
        dto.setStatus(order.getStatus().getCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.totalAmount());
        dto.setItems(order.getItems().stream().map(OrderRestMapper::toResponse).toList());
        dto.setShipments(order.getShipments().stream().map(OrderRestMapper::toResponse).toList());
        return dto;
    }

    public static List<OrderResponseDTO> toResponse(List<Order> orders) {
        return orders.stream().map(OrderRestMapper::toResponse).toList();
    }

    public static ShipmentResponseDTO toResponse(Shipment shipment) {
        ShipmentResponseDTO dto = new ShipmentResponseDTO();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrder().getId());
        dto.setOriginWarehouseId(shipment.getOriginWarehouse().getId());
        dto.setOriginWarehouseName(shipment.getOriginWarehouse().getName());
        dto.setDeliveryAddress(AddressRestMapper.toResponse(shipment.getDeliveryAddress()));
        dto.setLogisticsOperatorId(shipment.getLogisticsOperator().getId());
        dto.setStatus(shipment.getStatus().getCode());
        dto.setCreatedAt(shipment.getCreatedAt());
        return dto;
    }

    public static List<ShipmentResponseDTO> toShipmentResponses(List<Shipment> shipments) {
        return shipments.stream().map(OrderRestMapper::toResponse).toList();
    }

    private static OrderItemResponseDTO toResponse(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductType(item.getProduct().getType().getCode());
        dto.setVariant(VariantRestMapper.toResponse(item.getVariant()));
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.subtotal());
        dto.setOriginWarehouseId(item.getOriginWarehouse() != null ? item.getOriginWarehouse().getId() : null);
        return dto;
    }
}
