package nexusmarket.adapters.rest.openapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP error codes that an operation can answer, documented with the standard ErrorResponseDTO by
 * {@link OpenApiConfig}. Protected operations get 401 and 403 automatically, so they do not list them.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrors {

    int[] value();
}
