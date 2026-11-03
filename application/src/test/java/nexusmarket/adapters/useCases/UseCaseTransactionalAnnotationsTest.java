package nexusmarket.adapters.useCases;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.ports.in.BuyerPort;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.ports.in.PublicAccessPort;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.ports.in.SupervisorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the annotations of the use cases by reflection, from the methods of each role port. It checks
 * that they exist, not how they behave: TransactionalConsistencyIT covers the behavior.
 */
class UseCaseTransactionalAnnotationsTest {

    private static final Map<Class<?>, Class<?>> PORT_OF_USE_CASE = Map.of(
            PublicAccessUseCaseImpl.class, PublicAccessPort.class,
            BuyerUseCaseImpl.class, BuyerPort.class,
            SellerUseCaseImpl.class, SellerPort.class,
            LogisticsOperatorUseCaseImpl.class, LogisticsOperatorPort.class,
            AdministratorUseCaseImpl.class, AdministratorPort.class,
            SupervisorUseCaseImpl.class, SupervisorPort.class);

    static Stream<Class<?>> useCases() {
        return PORT_OF_USE_CASE.keySet().stream();
    }

    private static Stream<Arguments> portMethods(boolean consultations) {
        return PORT_OF_USE_CASE.entrySet().stream().flatMap(entry -> Arrays.stream(entry.getValue().getMethods())
                .filter(method -> method.getName().startsWith("consult") == consultations)
                .map(method -> Arguments.of(entry.getKey().getSimpleName() + "." + method.getName(),
                        implementationOf(entry.getKey(), method))));
    }

    static Stream<Arguments> consultationMethods() {
        return portMethods(true);
    }

    static Stream<Arguments> writeMethods() {
        return portMethods(false);
    }

    private static Method implementationOf(Class<?> useCase, Method portMethod) {
        try {
            return useCase.getMethod(portMethod.getName(), portMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The annotation Spring applies: the one on the method or, without one, the one on the class. */
    private static Transactional effectiveAnnotation(Method method) {
        Transactional onMethod = method.getAnnotation(Transactional.class);
        return onMethod != null ? onMethod : method.getDeclaringClass().getAnnotation(Transactional.class);
    }

    @ParameterizedTest
    @MethodSource("useCases")
    void useCaseIsTransactionalAtClassLevel(Class<?> useCase) {
        assertThat(useCase.getAnnotation(Transactional.class)).isNotNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("consultationMethods")
    void consultationIsReadOnly(String name, Method method) {
        assertThat(effectiveAnnotation(method).readOnly()).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writeMethods")
    void writeIsNotReadOnly(String name, Method method) {
        assertThat(effectiveAnnotation(method).readOnly()).isFalse();
    }

    @Test
    void rejectedPaymentDoesNotRollBackTheTransaction() throws NoSuchMethodException {
        Method validatePayment = BuyerUseCaseImpl.class.getMethod("validatePayment",
                nexusmarket.domain.models.User.class, String.class);

        assertThat(validatePayment.getAnnotation(Transactional.class).noRollbackFor())
                .containsExactly(PaymentRejectedException.class);
    }
}
