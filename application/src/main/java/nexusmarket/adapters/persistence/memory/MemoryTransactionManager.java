package nexusmarket.adapters.persistence.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Lets the @Transactional use cases run without a database, and fires the after-commit synchronizations
 * that write the audit trail.
 *
 * <p>There is no resource to commit or roll back. A rollback therefore does not undo changes: the memory
 * adapters keep the same objects they return, so an object already changed stays changed if the operation
 * fails later. See SDD/Adapters/Persistence-adapters.md.
 */
@Component
@Profile("memory")
public class MemoryTransactionManager extends AbstractPlatformTransactionManager {

    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // No resource to open.
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        // Nothing to write: the adapters already hold the objects.
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        // Nothing to undo: see the limitation in the class comment.
    }
}
