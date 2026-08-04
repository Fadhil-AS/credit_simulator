package id.co.bcadigital.credit.core.validation;

import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanInputParserTest {

    private final LoanInputParser parser = new LoanInputParser(2025);

    @Test
    void shouldParseVehicleTypeIgnoringCase() {
        assertEquals(VehicleType.MOBIL, parser.parseVehicleType("  mObIl "));
        assertEquals(VehicleType.MOTOR, parser.parseVehicleType("MOTOR"));
    }

    @Test
    void shouldRejectUnknownVehicleType() {
        assertThrows(ValidationException.class, () -> parser.parseVehicleType("truk"));
    }

    @Test
    void shouldParseVehicleConditionIgnoringCase() {
        assertEquals(VehicleCondition.BARU, parser.parseVehicleCondition("baru"));
        assertEquals(VehicleCondition.BEKAS, parser.parseVehicleCondition("Bekas"));
    }

    @Test
    @DisplayName("new vehicle cannot be older than currentYear - 1")
    void shouldRejectOldYearForNewVehicle() {
        assertEquals(2024, parser.parseVehicleYear("2024", VehicleCondition.BARU));
        assertThrows(ValidationException.class, () -> parser.parseVehicleYear("2023", VehicleCondition.BARU));
    }

    @Test
    @DisplayName("tahun tidak boleh melebihi tahun berjalan")
    void shouldRejectFutureYear() {
        assertEquals(2025, parser.parseVehicleYear("2025", VehicleCondition.BEKAS));
        assertThrows(ValidationException.class, () -> parser.parseVehicleYear("2026", VehicleCondition.BEKAS));
        assertThrows(ValidationException.class, () -> parser.parseVehicleYear("2026", VehicleCondition.BARU));
    }

    @Test
    void shouldRequireFourDigitYear() {
        assertThrows(ValidationException.class, () -> parser.parseVehicleYear("202", VehicleCondition.BEKAS));
        assertThrows(ValidationException.class, () -> parser.parseVehicleYear("20x5", VehicleCondition.BEKAS));
    }

    @Test
    void shouldCapLoanAmountAtOneBillion() {
        assertEquals(0, new BigDecimal("1000000000").compareTo(parser.parseLoanAmount("1000000000")));
        assertThrows(ValidationException.class, () -> parser.parseLoanAmount("1000000001"));
        assertThrows(ValidationException.class, () -> parser.parseLoanAmount("0"));
    }

    @Test
    void shouldAcceptTenureBetweenOneAndSix() {
        assertEquals(6, parser.parseTenure("6"));
        assertThrows(ValidationException.class, () -> parser.parseTenure("7"));
        assertThrows(ValidationException.class, () -> parser.parseTenure("0"));
    }

    @Test
    @DisplayName("down payment floor is 35% for Baru and 25% for Bekas")
    void shouldEnforceMinimumDownPayment() {
        BigDecimal loan = new BigDecimal("100000000");
        assertEquals(0, new BigDecimal("35000000")
                .compareTo(parser.parseDownPayment("35000000", loan, VehicleCondition.BARU)));
        assertThrows(ValidationException.class,
                () -> parser.parseDownPayment("34999999", loan, VehicleCondition.BARU));
        assertEquals(0, new BigDecimal("25000000")
                .compareTo(parser.parseDownPayment("25000000", loan, VehicleCondition.BEKAS)));
        assertThrows(ValidationException.class,
                () -> parser.parseDownPayment("24999999", loan, VehicleCondition.BEKAS));
    }

    @Test
    void shouldRejectDownPaymentCoveringWholeLoan() {
        BigDecimal loan = new BigDecimal("100000000");
        assertThrows(ValidationException.class,
                () -> parser.parseDownPayment("100000000", loan, VehicleCondition.BEKAS));
    }
}
