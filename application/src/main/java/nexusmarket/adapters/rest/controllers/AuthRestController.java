package nexusmarket.adapters.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.LoginResponseDTO;
import nexusmarket.adapters.rest.mappers.AddressRestMapper;
import nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.openapi.ApiErrors;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.PublicAccessPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and public registration (PublicAccessPort): login, logout and buyer self-registration.
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;
    private final long expiresInSeconds;

    public AuthRestController(PublicAccessPort publicAccessPort,
                              @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.publicAccessPort = publicAccessPort;
        this.expiresInSeconds = expirationMinutes * 60;
    }

    @Operation(summary = "Log in and get a token")
    @SecurityRequirements
    @ApiErrors({400, 401, 403})
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(UserRestMapper.toLoginResponse(
                publicAccessPort.login(request.getEmail(), request.getPassword()), expiresInSeconds));
    }

    @Operation(summary = "Log out and invalidate the tokens of the user")
    @ApiResponse(responseCode = "204", description = "Done, without content.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal(expression = "user") User user) {
        publicAccessPort.logout(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Register a new buyer")
    @ApiResponse(responseCode = "201", description = "Created.", useReturnTypeSchema = true)
    @SecurityRequirements
    @ApiErrors({400, 409})
    @PostMapping("/register/buyers")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody RegisterBuyerRequestDTO request) {
        Buyer buyer = publicAccessPort.registerBuyerUser(request.getIdentificationNumber(), request.getFullName(),
                request.getEmail(), request.getPassword(), AddressRestMapper.toDomain(request.getPrimaryAddress()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BuyerRestMapper.toResponse(buyer));
    }
}
