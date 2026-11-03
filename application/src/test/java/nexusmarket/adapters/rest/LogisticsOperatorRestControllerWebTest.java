package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.INITIAL_STOCK_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PASSWORD_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.QUANTITY_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.SHIPMENT;
import static nexusmarket.adapters.rest.RestResponseFixtures.SHIPMENT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.THRESHOLD;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.userJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.valueobjects.InventoryCondition;
import org.junit.jupiter.api.Test;

class LogisticsOperatorRestControllerWebTest extends EndpointWebTestBase<LogisticsOperatorPort> {

    private static final LogisticsOperator USER = DomainFixtures.operator();

    private static final EndpointCase.Routes<LogisticsOperatorPort> AS_OPERATOR = new EndpointCase.Routes<>(USER);

    @Override
    protected LogisticsOperatorPort port() {
        return logisticsOperatorPort;
    }

    static Stream<EndpointCase<LogisticsOperatorPort>> endpoints() {
        return Stream.of(
                AS_OPERATOR.get("/api/v1/logistics/account")
                        .stub(port -> when(port.consultUser(any(), any())).thenReturn(USER))
                        .calls(port -> verify(port).consultUser(same(USER), eq(USER.getId())))
                        .json(userJson(USER)),
                AS_OPERATOR.put("/api/v1/logistics/account/password")
                        .body(PASSWORD_BODY)
                        .calls(port -> verify(port).changeUserPassword(same(USER), eq("Current123*"), eq("Newpass123*")))
                        .status(204),
                AS_OPERATOR.get("/api/v1/logistics/warehouses")
                        .stub(port -> when(port.consultWarehouses(any())).thenReturn(List.of(WAREHOUSE)))
                        .calls(port -> verify(port).consultWarehouses(same(USER)))
                        .jsonList(WAREHOUSE_JSON),
                AS_OPERATOR.get("/api/v1/logistics/warehouses/{warehouseId}", "w-1")
                        .stub(port -> when(port.consultWarehouse(any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).consultWarehouse(same(USER), eq("w-1")))
                        .json(WAREHOUSE_JSON),
                AS_OPERATOR.post("/api/v1/logistics/inventories")
                        .body(INITIAL_STOCK_BODY)
                        .stub(port -> when(port.registerInitialStock(any(), any(), any(), anyInt())).thenReturn(INVENTORY))
                        .calls(port -> verify(port).registerInitialStock(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                AS_OPERATOR.post("/api/v1/logistics/inventories/{productId}/{warehouseId}/inbounds", "p-1", "w-1")
                        .body(QUANTITY_BODY)
                        .stub(port -> when(port.registerInventoryInbound(any(), any(), any(), anyInt())).thenReturn(INVENTORY))
                        .calls(port -> verify(port).registerInventoryInbound(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                adjustment().body("{\"quantity\": -2, \"condition\": \"DAMAGED\"}")
                        .calls(port -> verify(port).registerInventoryAdjustment(same(USER), eq("p-1"), eq("w-1"), eq(-2),
                                eq(InventoryCondition.DAMAGED))),
                AS_OPERATOR.post("/api/v1/logistics/inventories/{productId}/{warehouseId}/reservations", "p-1", "w-1")
                        .body(QUANTITY_BODY)
                        .stub(port -> when(port.reserveInventory(any(), any(), any(), anyInt())).thenReturn(INVENTORY))
                        .calls(port -> verify(port).reserveInventory(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                AS_OPERATOR.post("/api/v1/logistics/inventories/{productId}/{warehouseId}/reservation-releases", "p-1", "w-1")
                        .body(QUANTITY_BODY)
                        .stub(port -> when(port.releaseInventoryReservation(any(), any(), any(), anyInt()))
                                .thenReturn(INVENTORY))
                        .calls(port -> verify(port).releaseInventoryReservation(same(USER), eq("p-1"), eq("w-1"), eq(4)))
                        .status(201)
                        .json(INVENTORY_JSON),
                AS_OPERATOR.get("/api/v1/logistics/products/{productId}/inventories", "p-1")
                        .stub(port -> when(port.consultProductInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultProductInventory(same(USER), eq("p-1")))
                        .jsonList(INVENTORY_JSON),
                AS_OPERATOR.get("/api/v1/logistics/warehouses/{warehouseId}/inventories", "w-1")
                        .stub(port -> when(port.consultWarehouseInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultWarehouseInventory(same(USER), eq("w-1")))
                        .jsonList(INVENTORY_JSON),
                AS_OPERATOR.get("/api/v1/logistics/orders/{orderId}", "o-1")
                        .stub(port -> when(port.consultOrder(any(), any())).thenReturn(ORDER))
                        .calls(port -> verify(port).consultOrder(same(USER), eq("o-1")))
                        .json(ORDER_JSON),
                AS_OPERATOR.post("/api/v1/logistics/orders/{orderId}/shipments", "o-1")
                        .stub(port -> when(port.createShipments(any(), any())).thenReturn(List.of(SHIPMENT)))
                        .calls(port -> verify(port).createShipments(same(USER), eq("o-1")))
                        .status(201)
                        .jsonList(SHIPMENT_JSON),
                AS_OPERATOR.patch("/api/v1/logistics/shipments/{shipmentId}/pack", "s-1")
                        .stub(port -> when(port.packShipment(any(), any())).thenReturn(SHIPMENT))
                        .calls(port -> verify(port).packShipment(same(USER), eq("s-1")))
                        .json(SHIPMENT_JSON),
                AS_OPERATOR.patch("/api/v1/logistics/shipments/{shipmentId}/dispatch", "s-1")
                        .stub(port -> when(port.dispatchShipment(any(), any())).thenReturn(SHIPMENT))
                        .calls(port -> verify(port).dispatchShipment(same(USER), eq("s-1")))
                        .json(SHIPMENT_JSON),
                AS_OPERATOR.patch("/api/v1/logistics/shipments/{shipmentId}/deliver", "s-1")
                        .stub(port -> when(port.confirmDelivery(any(), any())).thenReturn(SHIPMENT))
                        .calls(port -> verify(port).confirmDelivery(same(USER), eq("s-1")))
                        .json(SHIPMENT_JSON),
                AS_OPERATOR.get("/api/v1/logistics/reports/inventory")
                        .param("lowStockThreshold", THRESHOLD)
                        .stub(port -> when(port.consultInventoryReport(any(), anyInt())).thenReturn(INVENTORY_REPORT))
                        .calls(port -> verify(port).consultInventoryReport(same(USER), eq(5)))
                        .json(INVENTORY_REPORT_JSON));
    }

    private static EndpointCase<LogisticsOperatorPort> adjustment() {
        return AS_OPERATOR.post("/api/v1/logistics/inventories/{productId}/{warehouseId}/adjustments", "p-1", "w-1")
                .stub(port -> when(port.registerInventoryAdjustment(any(), any(), any(), anyInt(), any()))
                        .thenReturn(INVENTORY))
                .status(201)
                .json(INVENTORY_JSON);
    }

    /** Second form of the same route, so it stays out of the endpoint table. */
    @Test
    void adjustmentWithoutConditionPassesNoCondition() throws Exception {
        delegatesToThePortWithExactArguments(adjustment().body("{\"quantity\": 3}")
                .calls(port -> verify(port).registerInventoryAdjustment(same(USER), eq("p-1"), eq("w-1"), eq(3),
                        isNull())));
    }
}
