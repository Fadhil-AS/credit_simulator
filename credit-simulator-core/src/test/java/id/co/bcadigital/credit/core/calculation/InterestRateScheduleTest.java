package id.co.bcadigital.credit.core.calculation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterestRateScheduleTest {

    private static final BigDecimal MOBIL_BASE_RATE = new BigDecimal("0.08");
    private static final BigDecimal MOTOR_BASE_RATE = new BigDecimal("0.09");

    @ParameterizedTest
    @CsvSource({
            "1, 0.08",
            "2, 0.081",
            "3, 0.086",
            "4, 0.087",
            "5, 0.092",
            "6, 0.093"
    })
    @DisplayName("mobil rate increases 0.1% every year and 0.5% every two years")
    void shouldProgressMobilRatePerYear(int year, String expected) {
        assertEquals(0, new BigDecimal(expected).compareTo(InterestRateSchedule.rateForYear(MOBIL_BASE_RATE, year)));
    }

    @Test
    void shouldProgressMotorRateFromNinePercentBase() {
        assertEquals(0, new BigDecimal("0.096").compareTo(InterestRateSchedule.rateForYear(MOTOR_BASE_RATE, 3)));
    }

    @Test
    void shouldRejectYearBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> InterestRateSchedule.rateForYear(MOBIL_BASE_RATE, 0));
    }
}
