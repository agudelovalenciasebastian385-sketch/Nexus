package nexusmarket.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import nexusmarket.domain.exceptions.InvalidCatalogCodeException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every business catalog (enum implementing DomainCatalog) resolves each of its codes with
 * {@code fromCode} and rejects an unknown code.
 */
class DomainCatalogTest {

    static Stream<Class<? extends DomainCatalog>> catalogs() {
        return Stream.of(AuditAction.class, AuditEntityType.class, CommercialStatus.class, InventoryCondition.class,
                MovementType.class, OrderStatus.class, PaymentResult.class, ProductStatus.class, ProductType.class,
                RefundStatus.class, ReturnStatus.class, ShipmentStatus.class, SystemRole.class, UserStatus.class,
                WarehouseType.class);
    }

    private static Object fromCode(Class<? extends DomainCatalog> catalog, String code) throws Throwable {
        Method fromCode = catalog.getMethod("fromCode", String.class);
        try {
            return fromCode.invoke(null, code);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @ParameterizedTest
    @MethodSource("catalogs")
    void everyCodeResolvesToItsConstant(Class<? extends DomainCatalog> catalog) throws Throwable {
        for (DomainCatalog constant : catalog.getEnumConstants()) {
            assertThat(fromCode(catalog, constant.getCode())).isSameAs(constant);
        }
    }

    @ParameterizedTest
    @MethodSource("catalogs")
    void unknownCodeIsRejected(Class<? extends DomainCatalog> catalog) {
        assertThatThrownBy(() -> fromCode(catalog, "UNKNOWN_CODE")).isInstanceOf(InvalidCatalogCodeException.class);
    }

    @ParameterizedTest
    @MethodSource("catalogs")
    void codesAreCaseSensitive(Class<? extends DomainCatalog> catalog) {
        String lowerCaseCode = catalog.getEnumConstants()[0].getCode().toLowerCase();

        assertThatThrownBy(() -> fromCode(catalog, lowerCaseCode)).isInstanceOf(InvalidCatalogCodeException.class);
    }
}
