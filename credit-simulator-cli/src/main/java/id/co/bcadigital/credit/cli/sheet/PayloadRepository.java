package id.co.bcadigital.credit.cli.sheet;

import id.co.bcadigital.credit.core.domain.LoanApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PayloadRepository {

    private static final String EXTENSION = ".json";

    private final Path directory;

    public PayloadRepository(Path directory) {
        this.directory = directory;
    }

    public static PayloadRepository defaultRepository() {
        String configured = System.getenv("CREDIT_SIMULATOR_MOCK_DIR");
        return configured == null || configured.isBlank()
                ? new PayloadRepository(Path.of("data", "mock"))
                : new PayloadRepository(Path.of(configured));
    }

    public Path directory() {
        return directory;
    }

    public Path publish(String name, LoanApplication application) {
        Path target = directory.resolve(SheetRepository.requireValidName(name) + EXTENSION);
        try {
            Files.createDirectories(directory);
            Files.writeString(target, toJson(application), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat menulis payload " + target, e);
        }
        return target;
    }

    public boolean delete(String name) {
        Path target = directory.resolve(SheetRepository.requireValidName(name) + EXTENSION);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat menghapus payload " + target, e);
        }
    }

    private static String toJson(LoanApplication application) {
        return """
                {
                  "vehicleType": "%s",
                  "vehicleCondition": "%s",
                  "vehicleYear": %d,
                  "totalLoanAmount": %s,
                  "loanTenure": %d,
                  "downPayment": %s
                }
                """.formatted(
                application.vehicle().type().displayName(),
                application.vehicle().condition().displayName(),
                application.vehicle().year(),
                plain(application.loanAmount()),
                application.tenureYears(),
                plain(application.downPayment()));
    }

    private static String plain(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
