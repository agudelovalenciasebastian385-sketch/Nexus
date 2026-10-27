package nexusmarket.domain.exceptions;

/** A report parameter or a computed report value is invalid. */
public class InvalidReportParameterException extends DomainException {

    public InvalidReportParameterException(String message) {
        super(message);
    }
}
