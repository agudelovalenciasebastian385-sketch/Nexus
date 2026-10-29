package nexusmarket.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import org.junit.jupiter.api.Test;

class ReportPeriodTest {

    private final ReportPeriod october = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    @Test
    void startAfterEndIsRejected() {
        assertThatThrownBy(() -> new ReportPeriod(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)))
                .isInstanceOf(InvalidReportPeriodException.class);
    }

    @Test
    void endDateIsRequired() {
        assertThatThrownBy(() -> new ReportPeriod(LocalDate.of(2026, 10, 1), null))
                .isInstanceOf(InvalidReportPeriodException.class);
    }

    @Test
    void singleDayPeriodIsAllowed() {
        LocalDate day = LocalDate.of(2026, 10, 1);

        assertThat(new ReportPeriod(day, day).endDateTimeExclusive()).isEqualTo(LocalDateTime.of(2026, 10, 2, 0, 0));
    }

    @Test
    void startDateTimeIsTheBeginningOfTheFirstDay() {
        assertThat(october.startDateTime()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
    }

    @Test
    void endDateTimeExclusiveIsTheBeginningOfTheDayAfterTheEndDate() {
        assertThat(october.endDateTimeExclusive()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void periodContainsItsFirstInstant() {
        assertThat(october.contains(LocalDateTime.of(2026, 10, 1, 0, 0))).isTrue();
    }

    @Test
    void periodContainsTheLastMomentOfItsLastDay() {
        assertThat(october.contains(LocalDateTime.of(2026, 10, 31, 23, 59, 59, 999_999_999))).isTrue();
    }

    @Test
    void periodDoesNotContainTheBeginningOfTheNextDay() {
        assertThat(october.contains(LocalDateTime.of(2026, 11, 1, 0, 0))).isFalse();
    }

    @Test
    void periodDoesNotContainTheMomentBeforeItsStart() {
        assertThat(october.contains(LocalDateTime.of(2026, 9, 30, 23, 59, 59))).isFalse();
    }
}
