package nexusmarket.adapters.persistence.memory;

import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

class MemoryTransactionManagerTest {

    private final TransactionTemplate transaction = new TransactionTemplate(new MemoryTransactionManager());
    private final AuditLogMemoryAdapter audit = new AuditLogMemoryAdapter();

    private static AuditLog auditLog(String id) {
        return new AuditLog(id, AuditAction.USER_LOGGED_IN, DomainFixtures.buyer(), AuditEntityType.USER, "u-1", NOW, Map.of());
    }

    @Test
    void theAuditRecordIsNotVisibleBeforeTheCommit() {
        List<AuditLog> during = transaction.execute(status -> {
            audit.save(auditLog("a-1"));
            return audit.findAll();
        });

        assertThat(during).isEmpty();
    }

    @Test
    void theAuditRecordIsWrittenAfterTheCommit() {
        transaction.executeWithoutResult(status -> audit.save(auditLog("a-1")));

        assertThat(audit.findAll()).hasSize(1);
    }

    @Test
    void anExceptionLeavesNoAuditRecord() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            audit.save(auditLog("a-1"));
            throw new IllegalStateException("failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(audit.findAll()).isEmpty();
    }

    @Test
    void aRollbackOnlyTransactionLeavesNoAuditRecord() {
        transaction.executeWithoutResult(status -> {
            audit.save(auditLog("a-1"));
            status.setRollbackOnly();
        });

        assertThat(audit.findAll()).isEmpty();
    }

    @Test
    void outsideATransactionTheAuditRecordIsWrittenAtOnce() {
        audit.save(auditLog("a-1"));

        assertThat(audit.findAll()).hasSize(1);
    }

    @Test
    void aTransactionIsActiveDuringTheCallback() {
        Boolean active = transaction.execute(status -> TransactionSynchronizationManager.isActualTransactionActive());

        assertThat(active).isTrue();
    }

    @Test
    void anInnerTransactionJoinsTheOuterOne() {
        Boolean isNew = transaction.execute(outer -> transaction.execute(inner -> inner.isNewTransaction()));

        assertThat(isNew).isFalse();
    }

    @Test
    void anInnerAuditRecordWaitsForTheOuterCommit() {
        List<AuditLog> afterInner = transaction.execute(outer -> {
            transaction.executeWithoutResult(inner -> audit.save(auditLog("a-1")));
            return audit.findAll();
        });

        assertThat(afterInner).isEmpty();
    }
}
