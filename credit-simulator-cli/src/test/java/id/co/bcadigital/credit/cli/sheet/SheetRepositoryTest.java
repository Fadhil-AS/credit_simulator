package id.co.bcadigital.credit.cli.sheet;

import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SheetRepositoryTest {

    @TempDir
    Path directory;

    @Test
    void shouldRoundTripASheet() {
        SheetRepository repository = new SheetRepository(directory);
        LoanApplication application = new LoanApplication(
                VehicleFactory.create(VehicleType.MOTOR, VehicleCondition.BEKAS, 2019),
                new BigDecimal("20000000"), 2, new BigDecimal("6000000"));

        repository.save("motor-2019", application);
        Optional<LoanApplication> reloaded = repository.find("motor-2019");

        assertTrue(reloaded.isPresent());
        assertEquals(VehicleType.MOTOR, reloaded.get().vehicle().type());
        assertEquals(VehicleCondition.BEKAS, reloaded.get().vehicle().condition());
        assertEquals(2019, reloaded.get().vehicle().year());
        assertEquals(2, reloaded.get().tenureYears());
        assertEquals(0, new BigDecimal("6000000").compareTo(reloaded.get().downPayment()));
    }

    @Test
    void shouldListSheetsSorted() {
        SheetRepository repository = new SheetRepository(directory);
        LoanApplication application = new LoanApplication(
                VehicleFactory.create(VehicleType.MOBIL, VehicleCondition.BEKAS, 2019),
                new BigDecimal("50000000"), 1, new BigDecimal("20000000"));
        repository.save("zeta", application);
        repository.save("alpha", application);

        assertEquals(List.of("alpha", "zeta"), repository.names());
    }

    @Test
    void shouldReturnEmptyForUnknownSheet() {
        assertTrue(new SheetRepository(directory).find("unknown").isEmpty());
        assertEquals(List.of(), new SheetRepository(directory.resolve("missing")).names());
    }

    @Test
    void shouldRejectSheetNamesThatEscapeTheDirectory() {
        SheetRepository repository = new SheetRepository(directory);

        assertThrows(IllegalArgumentException.class, () -> repository.find("../../etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> repository.find(""));
    }

    @Test
    void shouldDeleteSheet() {
        SheetRepository repository = new SheetRepository(directory);
        repository.save("draft", new LoanApplication(
                VehicleFactory.create(VehicleType.MOBIL, VehicleCondition.BEKAS, 2019),
                new BigDecimal("50000000"), 1, new BigDecimal("20000000")));

        assertTrue(repository.delete("draft"));
        assertFalse(repository.delete("draft"));
    }
}
