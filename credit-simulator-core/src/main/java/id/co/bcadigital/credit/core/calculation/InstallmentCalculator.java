package id.co.bcadigital.credit.core.calculation;

import id.co.bcadigital.credit.core.domain.InstallmentSchedule;
import id.co.bcadigital.credit.core.domain.LoanApplication;

public interface InstallmentCalculator {

    InstallmentSchedule calculate(LoanApplication application);
}
