package id.co.bcadigital.credit.core.validation;

import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleType;

import java.math.BigDecimal;
import java.time.Year;
import java.util.regex.Pattern;

public class LoanInputParser {

    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern AMOUNT = Pattern.compile("\\d+(\\.\\d{1,2})?");

    private final int currentYear;

    public LoanInputParser() {
        this(Year.now().getValue());
    }

    public LoanInputParser(int currentYear) {
        this.currentYear = currentYear;
    }

    public int currentYear() {
        return currentYear;
    }

    public VehicleType parseVehicleType(String raw) {
        VehicleType type = VehicleType.parse(raw);
        if (type == null) {
            throw new ValidationException("vehicleType", "Jenis kendaraan harus Motor atau Mobil.");
        }
        return type;
    }

    public VehicleCondition parseVehicleCondition(String raw) {
        VehicleCondition condition = VehicleCondition.parse(raw);
        if (condition == null) {
            throw new ValidationException("vehicleCondition", "Kondisi kendaraan harus Baru atau Bekas.");
        }
        return condition;
    }

    public int parseVehicleYear(String raw, VehicleCondition condition) {
        String value = trim(raw);
        if (value.length() != 4 || !DIGITS.matcher(value).matches()) {
            throw new ValidationException("vehicleYear", "Tahun kendaraan harus 4 digit angka.");
        }
        return validateVehicleYear(Integer.parseInt(value), condition);
    }

    public int validateVehicleYear(int year, VehicleCondition condition) {
        if (year < LoanRules.MIN_VEHICLE_YEAR || year > LoanRules.MAX_VEHICLE_YEAR) {
            throw new ValidationException("vehicleYear", "Tahun kendaraan harus 4 digit angka.");
        }
        if (year > currentYear) {
            throw new ValidationException("vehicleYear",
                    "Tahun kendaraan tidak boleh lebih dari " + currentYear + ".");
        }
        if (condition.isNew() && year < currentYear - 1) {
            throw new ValidationException("vehicleYear",
                    "Kendaraan Baru tidak boleh lebih tua dari tahun " + (currentYear - 1) + ".");
        }
        return year;
    }

    public BigDecimal parseLoanAmount(String raw) {
        String value = trim(raw);
        if (!AMOUNT.matcher(value).matches()) {
            throw new ValidationException("loanAmount", "Jumlah pinjaman harus berupa angka.");
        }
        return validateLoanAmount(new BigDecimal(value));
    }

    public BigDecimal validateLoanAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new ValidationException("loanAmount", "Jumlah pinjaman harus lebih besar dari nol.");
        }
        if (amount.compareTo(LoanRules.MAX_LOAN_AMOUNT) > 0) {
            throw new ValidationException("loanAmount", "Jumlah pinjaman maksimal 1 miliar.");
        }
        return amount;
    }

    public int parseTenure(String raw) {
        String value = trim(raw);
        if (!DIGITS.matcher(value).matches()) {
            throw new ValidationException("tenure", "Tenor pinjaman harus berupa angka.");
        }
        return validateTenure(Integer.parseInt(value));
    }

    public int validateTenure(int tenure) {
        if (tenure < LoanRules.MIN_TENURE_YEARS || tenure > LoanRules.MAX_TENURE_YEARS) {
            throw new ValidationException("tenure", "Tenor pinjaman harus antara "
                    + LoanRules.MIN_TENURE_YEARS + " sampai " + LoanRules.MAX_TENURE_YEARS + " tahun.");
        }
        return tenure;
    }

    public BigDecimal parseDownPayment(String raw, BigDecimal loanAmount, VehicleCondition condition) {
        String value = trim(raw);
        if (!AMOUNT.matcher(value).matches()) {
            throw new ValidationException("downPayment", "Jumlah DP harus berupa angka.");
        }
        return validateDownPayment(new BigDecimal(value), loanAmount, condition);
    }

    public BigDecimal validateDownPayment(BigDecimal downPayment, BigDecimal loanAmount, VehicleCondition condition) {
        if (downPayment.signum() < 0) {
            throw new ValidationException("downPayment", "Jumlah DP tidak boleh negatif.");
        }
        if (downPayment.compareTo(loanAmount) >= 0) {
            throw new ValidationException("downPayment", "Jumlah DP harus lebih kecil dari jumlah pinjaman.");
        }
        BigDecimal minimum = loanAmount.multiply(condition.minimumDownPaymentRatio());
        if (downPayment.compareTo(minimum) < 0) {
            throw new ValidationException("downPayment", "Jumlah DP kendaraan " + condition.displayName()
                    + " minimal " + condition.minimumDownPaymentRatio().movePointRight(2).stripTrailingZeros().toPlainString()
                    + "% dari jumlah pinjaman.");
        }
        return downPayment;
    }

    private static String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
