package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.ports.in.PublicAccessPort;

class PublicRestControllerWebTest extends EndpointWebTestBase<PublicAccessPort> {

    private static final EndpointCase.Routes<PublicAccessPort> ANONYMOUS = new EndpointCase.Routes<>(null);

    @Override
    protected PublicAccessPort port() {
        return publicAccessPort;
    }

    static Stream<EndpointCase<PublicAccessPort>> endpoints() {
        return Stream.of(
                ANONYMOUS.get("/api/v1/public/products")
                        .stub(port -> when(port.consultCatalog()).thenReturn(List.of(PRODUCT)))
                        .calls(port -> verify(port).consultCatalog())
                        .jsonList(PRODUCT_JSON),
                ANONYMOUS.get("/api/v1/public/products/{productId}", "p-1")
                        .stub(port -> when(port.consultCatalogProduct(any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).consultCatalogProduct(eq("p-1")))
                        .json(PRODUCT_JSON));
    }
}
