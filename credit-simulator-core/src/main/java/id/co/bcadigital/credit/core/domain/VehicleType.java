package id.co.bcadigital.credit.core.domain;

import java.util.Locale;

public enum VehicleType {

    MOBIL("Mobil"),
    MOTOR("Motor");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static VehicleType parse(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (VehicleType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
