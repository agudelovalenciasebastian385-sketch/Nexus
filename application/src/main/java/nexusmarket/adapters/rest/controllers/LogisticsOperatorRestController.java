package nexusmarket.adapters.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.InitialStockRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.InventoryAdjustmentRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.StockQuantityRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ShipmentResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import nexusmarket.adapters.rest.mappers.InventoryRestMapper;
import nexusmarket.adapters.rest.mappers.OrderRestMapper;
import nexusmarket.adapters.rest.mappers.ReportRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.mappers.WarehouseRestMapper;
import nexusmarket.adapters.rest.openapi.ApiErrors;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.valueobjects.InventoryCondition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Logistics operator endpoints (LogisticsOperatorPort, role LOGISTICS_OPERATOR): warehouses, inventory
 * movements of any product and warehouse, shipments and the inventory report. The account takes no id:
 * an operator only consults itself. Transactions are opened by LogisticsOperatorUseCaseImpl.
 */
@Tag(name = "Logistics operator")
@RestController
@RequestMapping("/api/v1/logistics")
public class LogisticsOperatorRestController {

    private final LogisticsOperatorPort logisticsOperatorPort;

    public LogisticsOperatorRestController(LogisticsOperatorPort logisticsOperatorPort) {
        this.logisticsOperatorPort = logisticsOperatorPort;
    }

    // ------------------------------------------------------------------ account

    @Operation(summary = "Get the own account")
    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> consultAccount(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(UserRestMapper.toResponse(logisticsOperatorPort.consultUser(user, user.getId())));
    }

    @Operation(summary = "Change the own password")
    @ApiResponse(responseCode = "204", description = "Done, without content.")
    @ApiErrors(400)
    @PutMapping("/account/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "user") User user,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        logisticsOperatorPort.changeUserPassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ warehouses

    @Operation(summary = "List the warehouses")
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(logisticsOperatorPort.consultWarehouses(user)));
    }

    @Operation(summary = "Get a warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> consultWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String warehouseId) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(logisticsOperatorPort.consultWarehouse(user, warehouseId)));
    }

    // ------------------------------------------------------------------ inventory

    @Operation(summary = "Register the initial stock of a product in a warehouse")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponseDTO> registerInitialStock(@AuthenticationPrincipal(expression = "user") User user,
                                                                     @Valid @RequestBody InitialStockRequestDTO request) {
        return created(logisticsOperatorPort.registerInitialStock(user, request.getProductId(),
                request.getWarehouseId(), request.getQuantity()));
    }

    @Operation(summary = "Register a stock inbound")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories/{productId}/{warehouseId}/inbounds")
    public ResponseEntity<InventoryResponseDTO> registerInbound(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable String productId,
                                                                @PathVariable String warehouseId,
                                                                @Valid @RequestBody StockQuantityRequestDTO request) {
        return created(logisticsOperatorPort.registerInventoryInbound(user, productId, warehouseId,
                request.getQuantity()));
    }

    @Operation(summary = "Register a stock adjustment")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories/{productId}/{warehouseId}/adjustments")
    public ResponseEntity<InventoryResponseDTO> registerAdjustment(@AuthenticationPrincipal(expression = "user") User user,
                                                                   @PathVariable String productId,
                                                                   @PathVariable String warehouseId,
                                                                   @Valid @RequestBody InventoryAdjustmentRequestDTO request) {
        InventoryCondition condition = request.getCondition() == null
                ? null
                : InventoryCondition.fromCode(request.getCondition());
        return created(logisticsOperatorPort.registerInventoryAdjustment(user, productId, warehouseId,
                request.getQuantity(), condition));
    }

    @Operation(summary = "Reserve stock")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories/{productId}/{warehouseId}/reservations")
    public ResponseEntity<InventoryResponseDTO> reserve(@AuthenticationPrincipal(expression = "user") User user,
                                                        @PathVariable String productId,
                                                        @PathVariable String warehouseId,
                                                        @Valid @RequestBody StockQuantityRequestDTO request) {
        return created(logisticsOperatorPort.reserveInventory(user, productId, warehouseId, request.getQuantity()));
    }

    @Operation(summary = "Release reserved stock")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories/{productId}/{warehouseId}/reservation-releases")
    public ResponseEntity<InventoryResponseDTO> releaseReservation(@AuthenticationPrincipal(expression = "user") User user,
                                                                   @PathVariable String productId,
                                                                   @PathVariable String warehouseId,
                                                                   @Valid @RequestBody StockQuantityRequestDTO request) {
        return created(logisticsOperatorPort.releaseInventoryReservation(user, productId, warehouseId,
                request.getQuantity()));
    }

    @Operation(summary = "List the stock of a product")
    @ApiErrors(404)
    @GetMapping("/products/{productId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultProductInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String productId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(
                logisticsOperatorPort.consultProductInventory(user, productId)));
    }

    @Operation(summary = "List the stock of a warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultWarehouseInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String warehouseId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(
                logisticsOperatorPort.consultWarehouseInventory(user, warehouseId)));
    }

    // ------------------------------------------------------------------ orders and shipments

    @Operation(summary = "Get an order")
    @ApiErrors(404)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> consultOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(logisticsOperatorPort.consultOrder(user, orderId)));
    }

    @Operation(summary = "Create the shipments of a paid order")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/orders/{orderId}/shipments")
    public ResponseEntity<List<ShipmentResponseDTO>> createShipments(@AuthenticationPrincipal(expression = "user") User user,
                                                                     @PathVariable String orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderRestMapper.toShipmentResponses(
                logisticsOperatorPort.createShipments(user, orderId)));
    }

    @Operation(summary = "Pack a shipment")
    @ApiErrors({404, 409})
    @PatchMapping("/shipments/{shipmentId}/pack")
    public ResponseEntity<ShipmentResponseDTO> packShipment(@AuthenticationPrincipal(expression = "user") User user,
                                                            @PathVariable String shipmentId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(logisticsOperatorPort.packShipment(user, shipmentId)));
    }

    @Operation(summary = "Dispatch a packed shipment")
    @ApiErrors({404, 409})
    @PatchMapping("/shipments/{shipmentId}/dispatch")
    public ResponseEntity<ShipmentResponseDTO> dispatchShipment(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable String shipmentId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(logisticsOperatorPort.dispatchShipment(user, shipmentId)));
    }

    @Operation(summary = "Confirm the delivery of a shipment")
    @ApiErrors({404, 409})
    @PatchMapping("/shipments/{shipmentId}/deliver")
    public ResponseEntity<ShipmentResponseDTO> confirmDelivery(@AuthenticationPrincipal(expression = "user") User user,
                                                               @PathVariable String shipmentId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(logisticsOperatorPort.confirmDelivery(user, shipmentId)));
    }

    // ------------------------------------------------------------------ report

    @Operation(summary = "Get the inventory report")
    @ApiErrors(400)
    @GetMapping("/reports/inventory")
    public ResponseEntity<InventoryReportResponseDTO> consultInventoryReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @Positive int lowStockThreshold) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                logisticsOperatorPort.consultInventoryReport(user, lowStockThreshold)));
    }

    private static ResponseEntity<InventoryResponseDTO> created(nexusmarket.domain.models.Inventory inventory) {
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponse(inventory));
    }
}
