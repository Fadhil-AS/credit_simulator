package id.co.bcadigital.credit.core.domain;

import java.math.BigDecimal;

public record YearlyInstallment(int year,
                                BigDecimal interestRate,
                                BigDecimal monthlyInstallment,
                                BigDecimal yearlyInstallment,
                                BigDecimal outstandingPrincipal) {
}
