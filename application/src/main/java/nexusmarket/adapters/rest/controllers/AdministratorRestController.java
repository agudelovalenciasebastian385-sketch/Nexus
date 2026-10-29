package nexusmarket.adapters.rest.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.ChangeCommercialStatusRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.ChangeUserStatusRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.RegisterSellerRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.RegisterStaffUserRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.UpdateSellerRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.WarehouseRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.AuditLogResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.CommercialReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InventoryReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.LogisticsReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerPerformanceReportResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerRegistrationResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import nexusmarket.adapters.rest.mappers.AddressRestMapper;
import nexusmarket.adapters.rest.mappers.AuditLogRestMapper;
import nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import nexusmarket.adapters.rest.mappers.InvoiceRestMapper;
import nexusmarket.adapters.rest.mappers.ProductRestMapper;
import nexusmarket.adapters.rest.mappers.ReportRestMapper;
import nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import nexusmarket.adapters.rest.mappers.SellerRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.mappers.WarehouseRestMapper;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
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
 * Administrator endpoints (AdministratorPort, role ADMINISTRATOR): users, sellers, buyers, Marketplace
 * warehouses, returns and refunds, audit and reports. An administrator only registers MARKETPLACE
 * warehouses, so the type is fixed here. Transactions are opened by AdministratorUseCaseImpl.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdministratorRestController {

    private final AdministratorPort administratorPort;

    public AdministratorRestController(AdministratorPort administratorPort) {
        this.administratorPort = administratorPort;
    }

    // ------------------------------------------------------------------ account and users

    @PutMapping("/account/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "user") User user,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        administratorPort.changeUserPassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff-users")
    public ResponseEntity<UserResponseDTO> registerStaffUser(@AuthenticationPrincipal(expression = "user") User user,
                                                             @Valid @RequestBody RegisterStaffUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserRestMapper.toResponse(
                administratorPort.registerStaffUser(user, SystemRole.fromCode(request.getRole()),
                        request.getIdentificationNumber(), request.getFullName(), request.getEmail(),
                        request.getPassword())));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> consultUser(@AuthenticationPrincipal(expression = "user") User user,
                                                       @PathVariable String userId) {
        return ResponseEntity.ok(UserRestMapper.toResponse(administratorPort.consultUser(user, userId)));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponseDTO> changeUserStatus(@AuthenticationPrincipal(expression = "user") User user,
                                                            @PathVariable String userId,
                                                            @Valid @RequestBody ChangeUserStatusRequestDTO request) {
        return ResponseEntity.ok(UserRestMapper.toResponse(administratorPort.changeUserStatus(user, userId,
                UserStatus.fromCode(request.getStatus()))));
    }

    // ------------------------------------------------------------------ sellers and buyers

    @PostMapping("/sellers")
    public ResponseEntity<SellerRegistrationResponseDTO> registerSeller(
            @AuthenticationPrincipal(expression = "user") User user, @Valid @RequestBody RegisterSellerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SellerRestMapper.toRegistrationResponse(
                administratorPort.registerSeller(user, request.getIdentificationNumber(), request.getFullName(),
                        request.getEmail(), request.getPassword(), request.getWarehouseName(),
                        AddressRestMapper.toDomain(request.getWarehouseLocation()))));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerResponseDTO> consultSeller(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String sellerId) {
        return ResponseEntity.ok(SellerRestMapper.toResponse(administratorPort.consultSeller(user, sellerId)));
    }

    @PatchMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerResponseDTO> updateSeller(@AuthenticationPrincipal(expression = "user") User user,
                                                          @PathVariable String sellerId,
                                                          @Valid @RequestBody UpdateSellerRequestDTO request) {
        return ResponseEntity.ok(SellerRestMapper.toResponse(
                administratorPort.updateSeller(user, sellerId, request.getFullName())));
    }

    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<List<ProductResponseDTO>> consultSellerProducts(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String sellerId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(administratorPort.consultSellerProducts(user, sellerId)));
    }

    @GetMapping("/buyers/{buyerId}")
    public ResponseEntity<BuyerResponseDTO> consultBuyer(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String buyerId) {
        return ResponseEntity.ok(BuyerRestMapper.toResponse(administratorPort.consultBuyer(user, buyerId)));
    }

    @PatchMapping("/buyers/{buyerId}/commercial-status")
    public ResponseEntity<BuyerResponseDTO> changeCommercialStatus(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String buyerId,
            @Valid @RequestBody ChangeCommercialStatusRequestDTO request) {
        return ResponseEntity.ok(BuyerRestMapper.toResponse(administratorPort.changeBuyerCommercialStatus(user,
                buyerId, CommercialStatus.fromCode(request.getCommercialStatus()))));
    }

    // ------------------------------------------------------------------ warehouses

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> registerWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @Valid @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(WarehouseRestMapper.toResponse(
                administratorPort.registerWarehouse(user, request.getName(),
                        AddressRestMapper.toDomain(request.getLocation()), WarehouseType.MARKETPLACE)));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(administratorPort.consultWarehouses(user)));
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> consultWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String warehouseId) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(administratorPort.consultWarehouse(user, warehouseId)));
    }

    @PutMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable String warehouseId,
                                                                @Valid @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponse(administratorPort.updateWarehouse(user, warehouseId,
                request.getName(), AddressRestMapper.toDomain(request.getLocation()))));
    }

    // ------------------------------------------------------------------ invoices, returns and refunds

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponseDTO> consultInvoice(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable String orderId) {
        return ResponseEntity.ok(InvoiceRestMapper.toResponse(administratorPort.consultInvoice(user, orderId)));
    }

    @GetMapping("/returns/{returnRequestId}")
    public ResponseEntity<ReturnRequestResponseDTO> consultReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(administratorPort.consultReturn(user, returnRequestId)));
    }

    @PatchMapping("/returns/{returnRequestId}/approve")
    public ResponseEntity<ReturnRequestResponseDTO> approveReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(administratorPort.approveReturn(user, returnRequestId)));
    }

    @PatchMapping("/returns/{returnRequestId}/reject")
    public ResponseEntity<ReturnRequestResponseDTO> rejectReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(administratorPort.rejectReturn(user, returnRequestId)));
    }

    @GetMapping("/returns/{returnRequestId}/refund")
    public ResponseEntity<RefundResponseDTO> consultRefund(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(administratorPort.consultRefund(user, returnRequestId)));
    }

    @PatchMapping("/refunds/{refundId}/process")
    public ResponseEntity<RefundResponseDTO> processRefund(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String refundId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(administratorPort.processRefund(user, refundId)));
    }

    // ------------------------------------------------------------------ audit

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogs(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(administratorPort.consultAuditLogs(user)));
    }

    @GetMapping("/audit-logs/users/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogsByUser(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String userId) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(administratorPort.consultAuditLogsByUser(user, userId)));
    }

    @GetMapping("/audit-logs/entities/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogsByEntity(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponse(administratorPort.consultAuditLogsByEntity(user,
                AuditEntityType.fromCode(entityType), entityId)));
    }

    // ------------------------------------------------------------------ reports

    @GetMapping("/reports/commercial")
    public ResponseEntity<CommercialReportResponseDTO> consultCommercialReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                administratorPort.consultCommercialReport(user, new ReportPeriod(startDate, endDate))));
    }

    @GetMapping("/reports/sellers/{sellerId}/performance")
    public ResponseEntity<SellerPerformanceReportResponseDTO> consultSellerPerformanceReport(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable String sellerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(administratorPort.consultSellerPerformanceReport(user,
                sellerId, new ReportPeriod(startDate, endDate))));
    }

    @GetMapping("/reports/inventory")
    public ResponseEntity<InventoryReportResponseDTO> consultInventoryReport(
            @AuthenticationPrincipal(expression = "user") User user, @RequestParam @Positive int lowStockThreshold) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                administratorPort.consultInventoryReport(user, lowStockThreshold)));
    }

    @GetMapping("/reports/logistics")
    public ResponseEntity<LogisticsReportResponseDTO> consultLogisticsReport(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ReportRestMapper.toResponse(
                administratorPort.consultLogisticsReport(user, new ReportPeriod(startDate, endDate))));
    }
}
