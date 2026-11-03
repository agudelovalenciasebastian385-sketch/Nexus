package nexusmarket.adapters.persistence.mongodb.mappers;

import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.junit.jupiter.api.Test;

class AuditLogMongoMapperTest {

    private final Seller seller = DomainFixtures.seller();
    private final AuditLog auditLog = new AuditLog("audit-1", AuditAction.PRODUCT_PUBLISHED, seller,
            AuditEntityType.PRODUCT, "product-1", NOW, Map.of("from", "DRAFT"));

    private AuditLog restored() {
        return AuditLogMongoMapper.toDomain(AuditLogMongoMapper.toDocument(auditLog), seller);
    }

    @Test
    void dataSurvivesTheRoundTrip() {
        assertThat(restored())
                .usingRecursiveComparison()
                .comparingOnlyFields("id", "action", "entityType", "entityId", "occurredAt", "details")
                .isEqualTo(auditLog);
    }

    @Test
    void roleIsTakenFromTheSuppliedUser() {
        assertThat(restored().getRole()).isEqualTo(seller.getRole());
    }

    @Test
    void documentStoresTheActingUserId() {
        assertThat(AuditLogMongoMapper.toDocument(auditLog).getPerformedById()).isEqualTo(seller.getId());
    }

    @Test
    void documentStoresTheActingUserEmail() {
        assertThat(AuditLogMongoMapper.toDocument(auditLog).getPerformedByEmail()).isEqualTo(seller.getEmail());
    }

    @Test
    void documentStoresTheRoleCode() {
        AuditLogDocument document = AuditLogMongoMapper.toDocument(auditLog);

        assertThat(document.getRole()).isEqualTo("SELLER");
    }

    @Test
    void documentStoresCatalogsAsCodes() {
        AuditLogDocument document = AuditLogMongoMapper.toDocument(auditLog);

        assertThat(document.getAction()).isEqualTo("PRODUCT_PUBLISHED");
        assertThat(document.getEntityType()).isEqualTo("PRODUCT");
    }
}
