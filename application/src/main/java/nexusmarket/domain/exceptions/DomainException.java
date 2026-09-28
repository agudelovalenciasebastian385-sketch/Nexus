package nexusmarket.domain.exceptions;

/**
 * Base class of every business exception of the NexusMarket domain.
 *
 * <p>Domain exceptions are unchecked and belong exclusively to the domain
 * layer; adapters translate them into technology-specific responses.</p>
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
