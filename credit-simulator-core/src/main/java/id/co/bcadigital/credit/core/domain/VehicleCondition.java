package id.co.bcadigital.credit.core.domain;

import java.math.BigDecimal;
import java.util.Locale;

public enum VehicleCondition {

    BARU("Baru", new BigDecimal("0.35")),
    BEKAS("Bekas", new BigDecimal("0.25"));

    private final String displayName;
    private final BigDecimal minimumDownPaymentRatio;

    VehicleCondition(String displayName, BigDecimal minimumDownPaymentRatio) {
        this.displayName = displayName;
        this.minimumDownPaymentRatio = minimumDownPaymentRatio;
    }

    public String displayName() {
        return displayName;
    }

    public BigDecimal minimumDownPaymentRatio() {
        return minimumDownPaymentRatio;
    }

    public boolean isNew() {
        return this == BARU;
    }

    public static VehicleCondition parse(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (VehicleCondition condition : values()) {
            if (condition.name().equals(normalized)) {
                return condition;
            }
        }
        return null;
    }
}
