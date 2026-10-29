package nexusmarket.domain.services.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.junit.jupiter.api.Test;

class AuditServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Administrator admin = h.admin();
    private final Seller seller = h.seller();

    private AuditLog recorded(AuditAction action, AuditEntityType type, String entityId) {
        return h.registerAuditLog.execute(seller, action, type, entityId, Map.of("key", "value"));
    }

    @Test
    void registeringStoresTheRecordWithTheUserRole() {
        AuditLog auditLog = recorded(AuditAction.PRODUCT_PUBLISHED, AuditEntityType.PRODUCT, "p-1");

        assertThat(h.audit.findAll()).containsExactly(auditLog);
        assertThat(auditLog.getRole()).isEqualTo(seller.getRole());
        assertThat(auditLog.getOccurredAt()).isNotNull();
    }

    @Test
    void administratorConsultsAllRecords() {
        recorded(AuditAction.PRODUCT_PUBLISHED, AuditEntityType.PRODUCT, "p-1");
        recorded(AuditAction.PRODUCT_SUSPENDED, AuditEntityType.PRODUCT, "p-1");

        assertThat(h.consultAuditLogs.consultAuditLogs(admin)).hasSize(2);
    }

    @Test
    void supervisorConsultsTheRecordsOfAUser() {
        recorded(AuditAction.PRODUCT_PUBLISHED, AuditEntityType.PRODUCT, "p-1");
        h.registerAuditLog.execute(admin, AuditAction.USER_STATUS_CHANGED, AuditEntityType.USER, "u-1", Map.of());

        assertThat(h.consultAuditLogs.consultAuditLogsByUser(h.supervisor(), seller.getId()))
                .extracting(AuditLog::getAction).containsExactly(AuditAction.PRODUCT_PUBLISHED);
    }

    @Test
    void recordsOfAnUnknownUserAreReported() {
        assertThatThrownBy(() -> h.consultAuditLogs.consultAuditLogsByUser(admin, "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void recordsAreFilteredByEntity() {
        recorded(AuditAction.PRODUCT_PUBLISHED, AuditEntityType.PRODUCT, "p-1");
        recorded(AuditAction.PRODUCT_PUBLISHED, AuditEntityType.PRODUCT, "p-2");

        assertThat(h.consultAuditLogs.consultAuditLogsByEntity(admin, AuditEntityType.PRODUCT, "p-2")).hasSize(1);
    }

    @Test
    void sellerCannotConsultTheAuditTrail() {
        assertThatThrownBy(() -> h.consultAuditLogs.consultAuditLogs(seller))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void blockedAdministratorCannotConsultTheAuditTrail() {
        h.blocked(admin);

        assertThatThrownBy(() -> h.consultAuditLogs.consultAuditLogs(admin))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
