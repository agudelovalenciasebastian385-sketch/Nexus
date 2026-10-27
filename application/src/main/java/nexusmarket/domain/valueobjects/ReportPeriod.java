package nexusmarket.domain.valueobjects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import nexusmarket.domain.exceptions.InvalidReportPeriodException;

/**
 * ReportPeriod (value object).
 *
 * <p>Range of calendar days covered by a report. Both dates are inclusive: a
 * period from {@code 2026-10-01} to {@code 2026-10-31} covers the whole month,
 * including every moment of October 31.</p>
 *
 * <p>Repository queries use the half-open interval
 * [{@link #startDateTime()}, {@link #endDateTimeExclusive()}), so every report
 * converts the period in the same way.</p>
 *
 * <p>Immutable and compared by value.</p>
 */
public final class ReportPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public ReportPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new InvalidReportPeriodException("Report period startDate is required.");
        }
        if (endDate == null) {
            throw new InvalidReportPeriodException("Report period endDate is required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidReportPeriodException(
                    "Report period startDate " + startDate + " cannot be after endDate " + endDate + ".");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /** Inclusive lower bound: the first moment of {@code startDate}. */
    public LocalDateTime startDateTime() {
        return startDate.atStartOfDay();
    }

    /** Exclusive upper bound: the first moment of the day after {@code endDate}. */
    public LocalDateTime endDateTimeExclusive() {
        return endDate.plusDays(1).atStartOfDay();
    }

    /** Indicates whether the moment falls inside the period. */
    public boolean contains(LocalDateTime moment) {
        return moment != null && !moment.isBefore(startDateTime()) && moment.isBefore(endDateTimeExclusive());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReportPeriod)) {
            return false;
        }
        ReportPeriod otherPeriod = (ReportPeriod) other;
        return startDate.equals(otherPeriod.startDate) && endDate.equals(otherPeriod.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return startDate + ".." + endDate;
    }
}
