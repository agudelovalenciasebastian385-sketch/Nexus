package nexusmarket.domain.exceptions;

/** Raised when a business code does not correspond to any value of a domain catalog. */
public class InvalidCatalogCodeException extends DomainException {

    public InvalidCatalogCodeException(String catalog, String code) {
        super("Unknown " + catalog + " code: " + code);
    }
}
