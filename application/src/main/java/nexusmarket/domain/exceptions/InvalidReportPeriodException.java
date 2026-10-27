package nexusmarket.domain.exceptions;

/** A report period is missing or its start date is after its end date. */
public class InvalidReportPeriodException extends DomainException {

    public InvalidReportPeriodException(String message) {
        super(message);
    }
}
