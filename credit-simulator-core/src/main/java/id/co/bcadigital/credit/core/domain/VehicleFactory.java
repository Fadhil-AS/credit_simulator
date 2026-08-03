package id.co.bcadigital.credit.core.domain;

import java.util.Objects;

public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(VehicleType type, VehicleCondition condition, int year) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(condition, "condition");
        return switch (type) {
            case MOBIL -> new Car(condition, year);
            case MOTOR -> new Motorcycle(condition, year);
        };
    }
}
