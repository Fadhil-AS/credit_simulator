package id.co.bcadigital.credit.core.calculation;

import id.co.bcadigital.credit.core.domain.InstallmentSchedule;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.YearlyInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DecliningBalanceInstallmentCalculator implements InstallmentCalculator {

    private static final int MONTHS_PER_YEAR = 12;
    private static final int MONEY_SCALE = 2;

    @Override
    public InstallmentSchedule calculate(LoanApplication application) {
        int tenure = application.tenureYears();
        BigDecimal baseRate = application.vehicle().baseInterestRate();
        BigDecimal outstanding = application.principal();
        List<YearlyInstallment> installments = new ArrayList<>(tenure);

        for (int year = 1; year <= tenure; year++) {
            BigDecimal rate = InterestRateSchedule.rateForYear(baseRate, year);
            BigDecimal payable = outstanding.multiply(BigDecimal.ONE.add(rate));
            int remainingMonths = MONTHS_PER_YEAR * (tenure - year + 1);
            BigDecimal monthly = payable.divide(BigDecimal.valueOf(remainingMonths), MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal yearly = monthly.multiply(BigDecimal.valueOf(MONTHS_PER_YEAR));

            installments.add(new YearlyInstallment(year, rate, monthly, yearly,
                    outstanding.setScale(MONEY_SCALE, RoundingMode.HALF_UP)));

            outstanding = payable.subtract(yearly);
        }
        return new InstallmentSchedule(application, installments);
    }
}
