package id.co.bcadigital.credit.core.validation;

import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.Vehicle;

public class LoanApplicationValidator {

    private final LoanInputParser parser;

    public LoanApplicationValidator() {
        this(new LoanInputParser());
    }

    public LoanApplicationValidator(LoanInputParser parser) {
        this.parser = parser;
    }

    public LoanApplication validate(LoanApplication application) {
        Vehicle vehicle = application.vehicle();
        parser.validateVehicleYear(vehicle.year(), vehicle.condition());
        parser.validateLoanAmount(application.loanAmount());
        parser.validateTenure(application.tenureYears());
        parser.validateDownPayment(application.downPayment(), application.loanAmount(), vehicle.condition());
        return application;
    }
}
