package id.co.bcadigital.credit.core.domain;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class Vehicle {

    private final VehicleCondition condition;
    private final int year;

    protected Vehicle(VehicleCondition condition, int year) {
        this.condition = Objects.requireNonNull(condition, "condition");
        this.year = year;
    }

    public abstract VehicleType type();

    public abstract BigDecimal baseInterestRate();

    public VehicleCondition condition() {
        return condition;
    }

    public int year() {
        return year;
    }

    public BigDecimal minimumDownPaymentRatio() {
        return condition.minimumDownPaymentRatio();
    }

    @Override
    public String toString() {
        return type().displayName() + " " + condition.displayName() + " " + year;
    }
}
