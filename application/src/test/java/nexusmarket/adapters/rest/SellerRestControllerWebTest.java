package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.END_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.FULL_NAME_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INITIAL_STOCK_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PASSWORD_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERIOD;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.QUANTITY_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_ADDRESS;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_VARIANT;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.START_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.VARIANT_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.userJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.WarehouseType;

class SellerRestControllerWebTest extends EndpointWebTestBase<SellerPort> {

    private static final Seller USER = DomainFixtures.seller();
    private static final String OWN_ID = USER.getId();
    private static final BigDecimal PRICE = new BigDecimal("150.50");
    private static final String PRODUCT_FIELDS = """
            "name": "Mouse", "description": "Ergonomic", "price": 150.50, "variants": [%s]""".formatted(VARIANT_BODY);

    private static final EndpointCase.Routes<SellerPort> AS_SELLER = new EndpointCase.Routes<>(USER);

    @Override
    protected SellerPort port() {
        return sellerPort;
    }

    static Stream<EndpointCase<SellerPort>> endpoints() {
        return Stream.of(
                AS_SELLER.get("/api/v1/seller/account")
                        .stub(port -> when(port.consultUser(any(), any())).thenReturn(USER))
                        .calls(port -> verify(port).consultUser(same(USER), eq(OWN_ID)))
                        .json(userJson(USER)),
                AS_SELLER.put("/api/v1/seller/account/password")
                        .body(PASSWORD_BODY)
                        .calls(port -> verify(port).changeUserPassword(same(USER), eq("Current123*"), eq("Newpass123*")))
                        .status(204),
                AS_SELLER.get("/api/v1/seller/profile")
                        .stub(port -> when(port.consultSeller(any(), any())).thenReturn(SELLER))
                        .calls(port -> verify(port).consultSeller(same(USER), eq(OWN_ID)))
                        .json(SELLER_JSON),
                AS_SELLER.patch("/api/v1/seller/profile")
                        .body(FULL_NAME_BODY)
                        .stub(port -> when(port.updateSeller(any(), any(), any())).thenReturn(SELLER))
                        .calls(port -> verify(port).updateSeller(same(USER), eq(OWN_ID), eq("New Name")))
                        .json(SELLER_JSON),
                AS_SELLER.post("/api/v1/seller/warehouses")
                        .body(WAREHOUSE_BODY)
                        .stub(port -> when(port.registerWarehouse(any(), any(), any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).registerWarehouse(same(USER), eq("Hub"), eq(REQUEST_ADDRESS),
                                eq(WarehouseType.SELLER)))
                        .status(201)
                        .json(WAREHOUSE_JSON),
                AS_SELLER.get("/api/v1/seller/warehouses")
                        .stub(port -> when(port.consultWarehouses(any())).thenReturn(List.of(WAREHOUSE)))
                        .calls(port -> verify(port).consultWarehouses(same(USER)))
                        .jsonList(WAREHOUSE_JSON),
                AS_SELLER.get("/api/v1/seller/warehouses/{warehouseId}", "w-1")
                        .stub(port -> when(port.consultWarehouse(any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).consultWarehouse(same(USER), eq("w-1")))
                        .json(WAREHOUSE_JSON),
                AS_SELLER.put("/api/v1/seller/warehouses/{warehouseId}", "w-1")
                        .body(WAREHOUSE_BODY)
                        .stub(port -> when(port.updateWarehouse(any(), any(), any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).updateWarehouse(same(USER), eq("w-1"), eq("Hub"), eq(REQUEST_ADDRESS)))
                        .json(WAREHOUSE_JSON),
                AS_SELLER.post("/api/v1/seller/products")
                        .body("{\"type\": \"PHYSICAL\", " + PRODUCT_FIELDS + "}")
                        .stub(port -> when(port.registerProduct(any(), any(), any(), any(), any(), any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).registerProduct(same(USER), eq(ProductType.PHYSICAL), eq("Mouse"),
                                eq("Ergonomic"), eq(PRICE), eq(List.of(REQUEST_VARIANT))))
                        .status(201)
                        .json(PRODUCT_JSON),
                AS_SELLER.get("/api/v1/seller/products")
                        .stub(port -> when(port.consultSellerProducts(any(), any())).thenReturn(List.of(PRODUCT)))
                        .calls(port -> verify(port).consultSellerProducts(same(USER), eq(OWN_ID)))
                        .jsonList(PRODUCT_JSON),
                AS_SELLER.put("/api/v1/seller/products/{productId}", "p-1")
                        .body("{" + PRODUCT_FIELDS + "}")
                        .stub(port -> when(port.updateProduct(any(), any(), any(), any(), any(), any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).updateProduct(same(USER), eq("p-1"), eq("Mouse"), eq("Ergonomic"),
                                eq(PRICE), eq(List.of(REQUEST_VARIANT))))
                        .json(PRODUCT_JSON),
                AS_SELLER.patch("/api/v1/seller/products/{productId}/publish", "p-1")
                        .stub(port -> when(port.publishProduct(any(), any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).publishProduct(same(USER), eq("p-1")))
                        .json(PRODUCT_JSON),
                AS_SELLER.patch("/api/v1/seller/products/{productId}/suspend", "p-1")
                        .stub(port -> when(port.suspendProduct(any(), any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).suspendProduct(same(USER), eq("p-1")))
                        .json(PRODUCT_JSON),
                AS_SELLER.patch("/api/v1/seller/products/{productId}/discontinue", "p-1")
                        .stub(port -> when(port.discontinueProduct(any(), any())).thenReturn(PRODUCT))
                        .calls(port -> verify(port).discontinueProduct(same(USER), eq("p-1")))
                        .json(PRODUCT_JSON),
                AS_SELLER.post("/api/v1/seller/inventories")
                        .body(INITIAL_STOCK_BODY)
                        .stub(port -> when(port.registerInitialStock(any(), any(), any(), anyInt())).thenReturn(INVENTORY))
                        .calls(port -> verify(port).registerInitialStock(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                AS_SELLER.post("/api/v1/seller/inventories/{productId}/{warehouseId}/inbounds", "p-1", "w-1")
                        .body(QUANTITY_BODY)
                        .stub(port -> when(port.registerInventoryInbound(any(), any(), any(), anyInt())).thenReturn(INVENTORY))
                        .calls(port -> verify(port).registerInventoryInbound(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                AS_SELLER.get("/api/v1/seller/products/{productId}/inventories", "p-1")
                        .stub(port -> when(port.consultProductInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultProductInventory(same(USER), eq("p-1")))
                        .jsonList(INVENTORY_JSON),
                AS_SELLER.get("/api/v1/seller/warehouses/{warehouseId}/inventories", "w-1")
                        .stub(port -> when(port.consultWarehouseInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultWarehouseInventory(same(USER), eq("w-1")))
                        .jsonList(INVENTORY_JSON),
                AS_SELLER.get("/api/v1/seller/orders/{orderId}", "o-1")
                        .stub(port -> when(port.consultOrder(any(), any())).thenReturn(ORDER))
                        .calls(port -> verify(port).consultOrder(same(USER), eq("o-1")))
                        .json(ORDER_JSON),
                AS_SELLER.get("/api/v1/seller/reports/performance")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultSellerPerformanceReport(any(), any(), any()))
                                .thenReturn(PERFORMANCE_REPORT))
                        .calls(port -> verify(port).consultSellerPerformanceReport(same(USER), eq(OWN_ID), eq(PERIOD)))
                        .json(PERFORMANCE_REPORT_JSON));
    }
}
