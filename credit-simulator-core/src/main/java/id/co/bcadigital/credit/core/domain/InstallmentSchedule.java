package id.co.bcadigital.credit.core.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public record InstallmentSchedule(LoanApplication application, List<YearlyInstallment> installments) {

    public InstallmentSchedule {
        Objects.requireNonNull(application, "application");
        installments = List.copyOf(installments);
    }

    public BigDecimal totalPayment() {
        return installments.stream()
                .map(YearlyInstallment::yearlyInstallment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalInterest() {
        return totalPayment().subtract(application.principal());
    }

    public BigDecimal averageMonthlyInstallment() {
        int months = application.tenureYears() * 12;
        return totalPayment().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }
}
