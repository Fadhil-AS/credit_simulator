package id.co.bcadigital.credit.core.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record LoanApplication(Vehicle vehicle, BigDecimal loanAmount, int tenureYears, BigDecimal downPayment) {

    public LoanApplication {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(loanAmount, "loanAmount");
        Objects.requireNonNull(downPayment, "downPayment");
    }

    public BigDecimal principal() {
        return loanAmount.subtract(downPayment);
    }

    public BigDecimal downPaymentRatio() {
        if (loanAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return downPayment.divide(loanAmount, 6, java.math.RoundingMode.HALF_UP);
    }
}
