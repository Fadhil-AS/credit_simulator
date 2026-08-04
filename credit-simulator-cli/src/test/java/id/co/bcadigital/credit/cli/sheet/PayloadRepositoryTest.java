package id.co.bcadigital.credit.cli.sheet;

import id.co.bcadigital.credit.cli.remote.JsonParser;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadRepositoryTest {

    @TempDir
    Path directory;

    private final LoanApplication application = new LoanApplication(
            VehicleFactory.create(VehicleType.MOTOR, VehicleCondition.BEKAS, 2020),
            new BigDecimal("30000000"), 4, new BigDecimal("9000000"));

    @Test
    @DisplayName("payload memakai skema yang sama dengan response endpoint pada soal")
    void shouldWritePayloadReadableByTheJsonParser() throws IOException {
        Path written = new PayloadRepository(directory).publish("motor-bekas", application);

        assertTrue(Files.exists(written));
        assertEquals("motor-bekas.json", written.getFileName().toString());

        Map<String, Object> payload = JsonParser.parseObject(Files.readString(written, StandardCharsets.UTF_8));
        assertEquals("Motor", payload.get("vehicleType"));
        assertEquals("Bekas", payload.get("vehicleCondition"));
        assertEquals(0, new BigDecimal("2020").compareTo((BigDecimal) payload.get("vehicleYear")));
        assertEquals(0, new BigDecimal("30000000").compareTo((BigDecimal) payload.get("totalLoanAmount")));
        assertEquals(0, new BigDecimal("4").compareTo((BigDecimal) payload.get("loanTenure")));
        assertEquals(0, new BigDecimal("9000000").compareTo((BigDecimal) payload.get("downPayment")));
    }

    @Test
    void shouldCreateDirectoryWhenMissing() {
        Path nested = directory.resolve("belum/ada");

        new PayloadRepository(nested).publish("baru", application);

        assertTrue(Files.isRegularFile(nested.resolve("baru.json")));
    }

    @Test
    void shouldDeletePayload() {
        PayloadRepository repository = new PayloadRepository(directory);
        repository.publish("sementara", application);

        assertTrue(repository.delete("sementara"));
        assertFalse(repository.delete("sementara"));
        assertFalse(Files.exists(directory.resolve("sementara.json")));
    }

    @Test
    void shouldRejectNamesThatEscapeTheDirectory() {
        PayloadRepository repository = new PayloadRepository(directory);

        assertThrows(IllegalArgumentException.class, () -> repository.publish("../rahasia", application));
        assertThrows(IllegalArgumentException.class, () -> repository.publish("", application));
    }
}
