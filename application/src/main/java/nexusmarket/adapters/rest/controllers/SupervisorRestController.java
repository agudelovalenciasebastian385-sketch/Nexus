package nexusmarket.adapters.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.AuditLogResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.CartResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.CommercialReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.LogisticsReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerPerformanceReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import nexusmarket.adapters.rest.mappers.AuditLogRestMapper;
import nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import nexusmarket.adapters.rest.mappers.CartRestMapper;
import nexusmarket.adapters.rest.mappers.InventoryRestMapper;
import nexusmarket.adapters.rest.mappers.InvoiceRestMapper;
import nexusmarket.adapters.rest.mappers.OrderRestMapper;
import nexusmarket.adapters.rest.mappers.ProductRestMapper;
import nexusmarket.adapters.rest.mappers.ReportRestMapper;
import nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import nexusmarket.adapters.rest.mappers.SellerRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.mappers.WarehouseRestMapper;
import nexusmarket.adapters.rest.openapi.ApiErrors;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.SupervisorPort;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supervisor endpoints (SupervisorPort, role SUPERVISOR): read-only access to the platform, plus the
 * change of its own password. Transactions are opened by SupervisorUseCaseImpl (read-only).
 */
@Tag(name = "Supervisor")
@RestController
@RequestMapping("/api/v1/supervisor")
public class SupervisorRestController {

    private final SupervisorPort supervisorPort;

    public SupervisorRestController(SupervisorPort supervisorPort) {
        this.supervisorPort = supervisorPort;
    }

    // ------------------------------------------------------------------ account and users

    @Operation(summary = "Change the own password")
    @ApiResponse(responseCode = "204", description = "Done, without content.")
    @ApiErrors(400)
    @PutMapping("/account/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "user") User user,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        supervisorPort.changeUserPassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a user")
    @ApiErrors(404)
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> consultUser(@AuthenticationPrincipal(expression = "user") User user,
                                                       @PathVariable String userId) {
        return ResponseEntity.ok(UserRestMapper.toResponse(supervisorPort.consultUser(user, userId)));
    }

    @Operation(summary = "Get a seller")
    @ApiErrors(404)
    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerResponseDTO> consultSeller(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String sellerId) {
        return ResponseEntity.ok(SellerRestMapper.toResponse(supervisorPort.consultSeller(user, sellerId)));
    }

    @Operation(summary = "Get a buyer")
    @ApiErrors(404)
    @GetMapping("/buyers/{buyerId}")
    public ResponseEntity<BuyerResponseDTO> consultBuyer(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String buyerId) {
        return ResponseEntity.ok(BuyerRestMapper.toResponse(supervisorPort.consultBuyer(user, buyerId)));
    }

    // ------------------------------------------------------------------ warehouses, products and inventory

    @Operation(summary = "List the warehouses")
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(supervisorPort.consultWarehouses(user)));
    }

    @Operation(summary = "Get a warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> consultWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String warehouseId) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(supervisorPort.consultWarehouse(user, warehouseId)));
    }

    @Operation(summary = "List the products of a seller")
    @ApiErrors(404)
    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<List<ProductResponseDTO>> consultSellerProducts(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String sellerId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(supervisorPort.consultSellerProducts(user, sellerId)));
    }

    @Operation(summary = "List the stock of a product")
    @ApiErrors(404)
    @GetMapping("/products/{productId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultProductInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String productId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(supervisorPort.consultProductInventory(user, productId)));
    }

    @Operation(summary = "List the stock of a warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultWarehouseInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String warehouseId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(
                supervisorPort.consultWarehouseInventory(user, warehouseId)));
    }

    // ------------------------------------------------------------------ buyers' carts and orders

    @Operation(summary = "Get the cart of a buyer")
    @ApiErrors(404)
    @GetMapping("/buyers/{buyerId}/cart")
    public ResponseEntity<CartResponseDTO> consultCart(@AuthenticationPrincipal(expression = "user") User user,
                                                       @PathVariable String buyerId) {
        return ResponseEntity.ok(CartRestMapper.toResponse(supervisorPort.consultCart(user, buyerId)));
    }

    @Operation(summary = "List the orders of a buyer")
    @ApiErrors(404)
    @GetMapping("/buyers/{buyerId}/orders")
    public ResponseEntity<List<OrderResponseDTO>> consultBuyerOrders(@AuthenticationPrincipal(expression = "user") User user,
                                                                     @PathVariable String buyerId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(supervisorPort.consultBuyerOrders(user, buyerId)));
    }

    @Operation(summary = "Get an order")
    @ApiErrors(404)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> consultOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(supervisorPort.consultOrder(user, orderId)));
    }

    @Operation(summary = "Get the invoice of an order")
    @ApiErrors(404)
    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponseDTO> consultInvoice(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable String orderId) {
        return ResponseEntity.ok(InvoiceRestMapper.toResponse(supervisorPort.consultInvoice(user, orderId)));
    }

    @Operation(summary = "Get a return request")
    @ApiErrors(404)
    @GetMapping("/returns/{returnRequestId}")
    public ResponseEntity<ReturnRequestResponseDTO> consultReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(supervisorPort.consultReturn(user, returnRequestId)));
    }

    @Operation(summary = "Get the refund of a return request")
    @ApiErrors(404)
    @GetMapping("/returns/{returnRequestId}/refund")
    public ResponseEntity<RefundResponseDTO> consultRefund(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(supervisorPort.consultRefund(user, returnRequestId)));
    }

    // ------------------------------------------------------------------ audit

    @Operation(summary = "List all the audit records")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogs(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(supervisorPort.consultAuditLogs(user)));
    }

    @Operation(summary = "List the audit records performed by a user")
    @ApiErrors(404)
    @GetMapping("/audit-logs/users/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogsByUser(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String userId) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(supervisorPort.consultAuditLogsByUser(user, userId)));
    }

    @Operation(summary = "List the audit records of an entity")
    @ApiErrors(400)
    @GetMapping("/audit-logs/entities/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogsByEntity(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(supervisorPort.consultAuditLogsByEntity(user,
                AuditEntityType.fromCode(entityType), entityId)));
    }

    // ------------------------------------------------------------------ reports

    @Operation(summary = "Get the commercial report of a period")
    @ApiErrors(400)
    @GetMapping("/reports/commercial")
    public ResponseEntity<CommercialReportResponseDTO> consultCommercialReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                supervisorPort.consultCommercialReport(user, new ReportPeriod(startDate, endDate))));
    }

    @Operation(summary = "Get the performance report of a seller in a period")
    @ApiErrors({400, 404})
    @GetMapping("/reports/sellers/{sellerId}/performance")
    public ResponseEntity<SellerPerformanceReportResponseDTO> consultSellerPerformanceReport(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String sellerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(supervisorPort.consultSellerPerformanceReport(user,
                sellerId, new ReportPeriod(startDate, endDate))));
    }

    @Operation(summary = "Get the inventory report")
    @ApiErrors(400)
    @GetMapping("/reports/inventory")
    public ResponseEntity<InventoryReportResponseDTO> consultInventoryReport(
            @AuthenticationPrincipal(expression = "user") User user, @RequestParam @Positive int lowStockThreshold) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(supervisorPort.consultInventoryReport(user, lowStockThreshold)));
    }

    @Operation(summary = "Get the logistics report of a period")
    @ApiErrors(400)
    @GetMapping("/reports/logistics")
    public ResponseEntity<LogisticsReportResponseDTO> consultLogisticsReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                supervisorPort.consultLogisticsReport(user, new ReportPeriod(startDate, endDate))));
    }
}
