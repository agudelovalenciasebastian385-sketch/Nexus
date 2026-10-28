package nexusmarket.adapters.persistence.mongodb.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the AuditLog persistence document.
 */
public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findAllByOrderByOccurredAtAsc();

    List<AuditLogDocument> findByPerformedByIdOrderByOccurredAtAsc(String performedById);

    List<AuditLogDocument> findByEntityTypeAndEntityIdOrderByOccurredAtAsc(String entityType, String entityId);
}
