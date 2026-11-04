package nexusmarket.adapters.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import nexusmarket.adapters.rest.mappers.ProductRestMapper;
import nexusmarket.adapters.rest.openapi.ApiErrors;
import nexusmarket.domain.ports.in.PublicAccessPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public catalog (PublicAccessPort), open without authentication.
 */
@Tag(name = "Public catalog")
@RestController
@RequestMapping("/api/v1/public")
public class PublicRestController {

    private final PublicAccessPort publicAccessPort;

    public PublicRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @Operation(summary = "List the published products")
    @SecurityRequirements
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> consultCatalog() {
        return ResponseEntity.ok(ProductRestMapper.toResponse(publicAccessPort.consultCatalog()));
    }

    @Operation(summary = "Get a published product")
    @SecurityRequirements
    @ApiErrors(404)
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDTO> consultCatalogProduct(@PathVariable String productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponse(publicAccessPort.consultCatalogProduct(productId)));
    }
}
