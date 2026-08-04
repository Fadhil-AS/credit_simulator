package id.co.bcadigital.credit.core.calculation;

import java.math.BigDecimal;

public final class InterestRateSchedule {

    private static final BigDecimal YEARLY_INCREMENT = new BigDecimal("0.001");
    private static final BigDecimal BIENNIAL_INCREMENT = new BigDecimal("0.005");

    private InterestRateSchedule() {
    }

    public static BigDecimal rateForYear(BigDecimal baseRate, int year) {
        if (year < 1) {
            throw new IllegalArgumentException("year must be greater than zero");
        }
        BigDecimal rate = baseRate;
        for (int current = 2; current <= year; current++) {
            rate = rate.add(current % 2 == 0 ? YEARLY_INCREMENT : BIENNIAL_INCREMENT);
        }
        return rate;
    }
}
