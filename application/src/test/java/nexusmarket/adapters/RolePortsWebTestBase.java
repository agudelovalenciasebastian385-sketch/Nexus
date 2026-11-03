package nexusmarket.adapters;

import static org.mockito.Mockito.verifyNoInteractions;

import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.ports.in.BuyerPort;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.ports.in.PublicAccessPort;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.ports.in.SupervisorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base of the {@code @WebMvcTest} classes: the web layer (controllers, validation, exception handler)
 * without a database. The six role ports are Mockito mocks; Mockito is used only in these web tests.
 */
public abstract class RolePortsWebTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected PublicAccessPort publicAccessPort;

    @MockitoBean
    protected BuyerPort buyerPort;

    @MockitoBean
    protected SellerPort sellerPort;

    @MockitoBean
    protected LogisticsOperatorPort logisticsOperatorPort;

    @MockitoBean
    protected AdministratorPort administratorPort;

    @MockitoBean
    protected SupervisorPort supervisorPort;

    protected void verifyNoRolePortWasCalled() {
        verifyNoInteractions(publicAccessPort, buyerPort, sellerPort, logisticsOperatorPort, administratorPort,
                supervisorPort);
    }
}
