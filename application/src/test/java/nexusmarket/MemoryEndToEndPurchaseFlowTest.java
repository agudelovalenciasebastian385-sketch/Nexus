package nexusmarket;

import org.springframework.test.context.ActiveProfiles;

/**
 * The scenarios of EndToEndPurchaseFlowIT over the memory adapters, without Docker: both profiles must
 * behave the same through the API.
 */
@ActiveProfiles(value = "memory", inheritProfiles = false)
class MemoryEndToEndPurchaseFlowTest extends EndToEndPurchaseFlowIT {
}
