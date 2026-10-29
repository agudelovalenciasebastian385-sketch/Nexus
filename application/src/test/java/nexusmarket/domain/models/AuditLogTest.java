package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.seller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import nexusmarket.domain.exceptions.InvalidAuditLogException;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;

class AuditLogTest {

    private AuditLog auditLog(Map<String, String> details) {
        return new AuditLog("audit-1", AuditAction.PRODUCT_PUBLISHED, seller(), AuditEntityType.PRODUCT, "product-1",
                NOW, details);
    }

    @Test
    void roleIsTakenFromThePerformingUser() {
        assertThat(auditLog(Map.of()).getRole()).isEqualTo(SystemRole.SELLER);
    }

    @Test
    void detailsAreACopyThatCannotBeModified() {
        Map<String, String> details = new HashMap<>(Map.of("from", "DRAFT"));
        AuditLog auditLog = auditLog(details);

        details.put("to", "PUBLISHED");

        assertThat(auditLog.getDetails()).containsOnlyKeys("from");
    }

    @Test
    void missingDetailsBecomeEmpty() {
        assertThat(auditLog(null).getDetails()).isEmpty();
    }

    @Test
    void entityIdIsRequired() {
        assertThatThrownBy(() -> new AuditLog("audit-1", AuditAction.PRODUCT_PUBLISHED, seller(),
                AuditEntityType.PRODUCT, " ", NOW, Map.of())).isInstanceOf(InvalidAuditLogException.class);
    }
}
