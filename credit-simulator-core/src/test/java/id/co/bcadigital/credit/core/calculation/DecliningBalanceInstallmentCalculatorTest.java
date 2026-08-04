package id.co.bcadigital.credit.core.calculation;

import id.co.bcadigital.credit.core.domain.InstallmentSchedule;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.Vehicle;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;
import id.co.bcadigital.credit.core.domain.YearlyInstallment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecliningBalanceInstallmentCalculatorTest {

    private final InstallmentCalculator calculator = new DecliningBalanceInstallmentCalculator();

    @Test
    @DisplayName("reproduces the reference numbers of Rumus.xlsx")
    void shouldMatchReferenceSpreadsheet() {
        Vehicle car = VehicleFactory.create(VehicleType.MOBIL, VehicleCondition.BEKAS, 2023);
        LoanApplication application = new LoanApplication(car,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        InstallmentSchedule schedule = calculator.calculate(application);

        assertEquals(3, schedule.installments().size());
        assertMonthly(schedule.installments().get(0), "0.08", "2250000.00");
        assertMonthly(schedule.installments().get(1), "0.081", "2432250.00");
        assertMonthly(schedule.installments().get(2), "0.086", "2641423.50");
    }

    @Test
    void shouldChargeMotorWithNinePercentBaseRate() {
        Vehicle motorcycle = VehicleFactory.create(VehicleType.MOTOR, VehicleCondition.BARU, 2025);
        LoanApplication application = new LoanApplication(motorcycle,
                new BigDecimal("20000000"), 1, new BigDecimal("7000000"));

        InstallmentSchedule schedule = calculator.calculate(application);

        assertMonthly(schedule.installments().get(0), "0.09", "1180833.33");
    }

    @Test
    void shouldExposeTotalsDerivedFromYearlyInstallments() {
        Vehicle car = VehicleFactory.create(VehicleType.MOBIL, VehicleCondition.BEKAS, 2023);
        LoanApplication application = new LoanApplication(car,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        InstallmentSchedule schedule = calculator.calculate(application);

        assertEquals(0, new BigDecimal("87884082.00").compareTo(schedule.totalPayment()));
        assertEquals(0, new BigDecimal("12884082.00").compareTo(schedule.totalInterest()));
        assertEquals(0, new BigDecimal("2441224.50").compareTo(schedule.averageMonthlyInstallment()));
    }

    private static void assertMonthly(YearlyInstallment installment, String rate, String monthly) {
        assertEquals(0, new BigDecimal(rate).compareTo(installment.interestRate()));
        assertEquals(0, new BigDecimal(monthly).compareTo(installment.monthlyInstallment()));
    }
}
