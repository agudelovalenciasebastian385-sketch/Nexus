package nexusmarket.adapters.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.InitialStockRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.RegisterProductRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.StockQuantityRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.UpdateProductRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.UpdateSellerRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.WarehouseRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerPerformanceReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import nexusmarket.adapters.rest.mappers.AddressRestMapper;
import nexusmarket.adapters.rest.mappers.InventoryRestMapper;
import nexusmarket.adapters.rest.mappers.OrderRestMapper;
import nexusmarket.adapters.rest.mappers.ProductRestMapper;
import nexusmarket.adapters.rest.mappers.ReportRestMapper;
import nexusmarket.adapters.rest.mappers.SellerRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.mappers.VariantRestMapper;
import nexusmarket.adapters.rest.mappers.WarehouseRestMapper;
import nexusmarket.adapters.rest.openapi.ApiErrors;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.format.annotation.DateTimeFormat;
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
 * Seller endpoints (SellerPort, role SELLER). Own resources (account, profile, products list,
 * performance report) take no id in the path: the controller passes the authenticated user's id and
 * the services still validate ownership (RG-03). A seller only registers SELLER warehouses, so the
 * type is fixed here. Transactions are opened by SellerUseCaseImpl.
 */
@Tag(name = "Seller")
@RestController
@RequestMapping("/api/v1/seller")
public class SellerRestController {

    private final SellerPort sellerPort;

    public SellerRestController(SellerPort sellerPort) {
        this.sellerPort = sellerPort;
    }

    // ------------------------------------------------------------------ account and profile

    @Operation(summary = "Get the own account")
    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> consultAccount(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(UserRestMapper.toResponse(sellerPort.consultUser(user, user.getId())));
    }

    @Operation(summary = "Change the own password")
    @ApiResponse(responseCode = "204", description = "Done, without content.")
    @ApiErrors(400)
    @PutMapping("/account/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "user") User user,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        sellerPort.changeUserPassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get the own profile")
    @GetMapping("/profile")
    public ResponseEntity<SellerResponseDTO> consultProfile(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(SellerRestMapper.toResponse(sellerPort.consultSeller(user, user.getId())));
    }

    @Operation(summary = "Update the own profile")
    @ApiErrors(400)
    @PatchMapping("/profile")
    public ResponseEntity<SellerResponseDTO> updateProfile(@AuthenticationPrincipal(expression = "user") User user,
                                                           @Valid @RequestBody UpdateSellerRequestDTO request) {
        return ResponseEntity.ok(SellerRestMapper.toResponse(
                sellerPort.updateSeller(user, user.getId(), request.getFullName())));
    }

    // ------------------------------------------------------------------ warehouses

    @Operation(summary = "Register an own warehouse")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors(400)
    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> registerWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @Valid @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(WarehouseRestMapper.toResponse(
                sellerPort.registerWarehouse(user, request.getName(), AddressRestMapper.toDomain(request.getLocation()),
                        WarehouseType.SELLER)));
    }

    @Operation(summary = "List the own warehouses")
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(sellerPort.consultWarehouses(user)));
    }

    @Operation(summary = "Get an own warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> consultWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String warehouseId) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(sellerPort.consultWarehouse(user, warehouseId)));
    }

    @Operation(summary = "Update an own warehouse")
    @ApiErrors({400, 404})
    @PutMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable String warehouseId,
                                                                @Valid @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(sellerPort.updateWarehouse(user, warehouseId,
                request.getName(), AddressRestMapper.toDomain(request.getLocation()))));
    }

    // ------------------------------------------------------------------ products

    @Operation(summary = "Register a product in draft status")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors(400)
    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> registerProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                              @Valid @RequestBody RegisterProductRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductRestMapper.toResponse(
                sellerPort.registerProduct(user, ProductType.fromCode(request.getType()), request.getName(),
                        request.getDescription(), request.getPrice(),
                        VariantRestMapper.toDomain(request.getVariants()))));
    }

    @Operation(summary = "List the own products")
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> consultProducts(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(sellerPort.consultSellerProducts(user, user.getId())));
    }

    @Operation(summary = "Update an own product")
    @ApiErrors({400, 404})
    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                            @PathVariable String productId,
                                                            @Valid @RequestBody UpdateProductRequestDTO request) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(sellerPort.updateProduct(user, productId,
                request.getName(), request.getDescription(), request.getPrice(),
                VariantRestMapper.toDomain(request.getVariants()))));
    }

    @Operation(summary = "Publish an own product")
    @ApiErrors({404, 409})
    @PatchMapping("/products/{productId}/publish")
    public ResponseEntity<ProductResponseDTO> publishProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable String productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(sellerPort.publishProduct(user, productId)));
    }

    @Operation(summary = "Suspend an own product")
    @ApiErrors({404, 409})
    @PatchMapping("/products/{productId}/suspend")
    public ResponseEntity<ProductResponseDTO> suspendProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable String productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(sellerPort.suspendProduct(user, productId)));
    }

    @Operation(summary = "Discontinue an own product")
    @ApiErrors({404, 409})
    @PatchMapping("/products/{productId}/discontinue")
    public ResponseEntity<ProductResponseDTO> discontinueProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(sellerPort.discontinueProduct(user, productId)));
    }

    // ------------------------------------------------------------------ inventory

    @Operation(summary = "Register the initial stock of an own product")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404})
    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponseDTO> registerInitialStock(@AuthenticationPrincipal(expression = "user") User user,
                                                                     @Valid @RequestBody InitialStockRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponse(
                sellerPort.registerInitialStock(user, request.getProductId(), request.getWarehouseId(),
                        request.getQuantity())));
    }

    @Operation(summary = "Register a stock inbound of an own product")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @ApiErrors({400, 404, 409})
    @PostMapping("/inventories/{productId}/{warehouseId}/inbounds")
    public ResponseEntity<InventoryResponseDTO> registerInbound(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable String productId,
                                                                @PathVariable String warehouseId,
                                                                @Valid @RequestBody StockQuantityRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponse(
                sellerPort.registerInventoryInbound(user, productId, warehouseId, request.getQuantity())));
    }

    @Operation(summary = "List the stock of an own product")
    @ApiErrors(404)
    @GetMapping("/products/{productId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultProductInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String productId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(sellerPort.consultProductInventory(user, productId)));
    }

    @Operation(summary = "List the stock of an own warehouse")
    @ApiErrors(404)
    @GetMapping("/warehouses/{warehouseId}/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultWarehouseInventory(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String warehouseId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponse(sellerPort.consultWarehouseInventory(user, warehouseId)));
    }

    // ------------------------------------------------------------------ orders and report

    @Operation(summary = "Get an order that contains own products")
    @ApiErrors(404)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> consultOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(sellerPort.consultOrder(user, orderId)));
    }

    @Operation(summary = "Get the own performance report of a period")
    @ApiErrors(400)
    @GetMapping("/reports/performance")
    public ResponseEntity<SellerPerformanceReportResponseDTO> consultPerformanceReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(sellerPort.consultSellerPerformanceReport(user,
                user.getId(), new ReportPeriod(startDate, endDate))));
    }
}
