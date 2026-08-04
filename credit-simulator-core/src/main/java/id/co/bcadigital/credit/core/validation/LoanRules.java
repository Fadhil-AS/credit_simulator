package id.co.bcadigital.credit.core.validation;

import java.math.BigDecimal;

public final class LoanRules {

    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000");
    public static final int MIN_TENURE_YEARS = 1;
    public static final int MAX_TENURE_YEARS = 6;
    public static final int MIN_VEHICLE_YEAR = 1000;
    public static final int MAX_VEHICLE_YEAR = 9999;

    private LoanRules() {
    }
}
