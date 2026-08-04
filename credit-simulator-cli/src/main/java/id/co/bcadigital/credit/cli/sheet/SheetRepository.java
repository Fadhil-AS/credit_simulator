package id.co.bcadigital.credit.cli.sheet;

import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

public class SheetRepository {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,39}");
    private static final String EXTENSION = ".sheet";

    private final Path directory;

    public SheetRepository(Path directory) {
        this.directory = directory;
    }

    public static SheetRepository defaultRepository() {
        String configured = System.getenv("CREDIT_SIMULATOR_SHEET_DIR");
        return configured == null || configured.isBlank()
                ? new SheetRepository(Path.of("data", "sheets"))
                : new SheetRepository(Path.of(configured));
    }

    public void save(String name, LoanApplication application) {
        String sheetName = requireValidName(name);
        Properties properties = new Properties();
        properties.setProperty("vehicleType", application.vehicle().type().name());
        properties.setProperty("vehicleCondition", application.vehicle().condition().name());
        properties.setProperty("vehicleYear", String.valueOf(application.vehicle().year()));
        properties.setProperty("loanAmount", application.loanAmount().toPlainString());
        properties.setProperty("tenureYears", String.valueOf(application.tenureYears()));
        properties.setProperty("downPayment", application.downPayment().toPlainString());
        try {
            Files.createDirectories(directory);
            try (OutputStream out = Files.newOutputStream(pathOf(sheetName))) {
                properties.store(out, "credit simulator sheet " + sheetName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat menyimpan sheet " + sheetName, e);
        }
    }

    public Optional<LoanApplication> find(String name) {
        Path path = pathOf(requireValidName(name));
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat membaca sheet " + name, e);
        }
        return Optional.of(new LoanApplication(
                VehicleFactory.create(
                        VehicleType.valueOf(properties.getProperty("vehicleType")),
                        VehicleCondition.valueOf(properties.getProperty("vehicleCondition")),
                        Integer.parseInt(properties.getProperty("vehicleYear"))),
                new BigDecimal(properties.getProperty("loanAmount")),
                Integer.parseInt(properties.getProperty("tenureYears")),
                new BigDecimal(properties.getProperty("downPayment"))));
    }

    public List<String> names() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                names.add(fileName.substring(0, fileName.length() - EXTENSION.length()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat membaca daftar sheet", e);
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public boolean delete(String name) {
        try {
            return Files.deleteIfExists(pathOf(requireValidName(name)));
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat menghapus sheet " + name, e);
        }
    }

    private Path pathOf(String name) {
        return directory.resolve(name + EXTENSION);
    }

    static String requireValidName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (!VALID_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Nama sheet hanya boleh huruf, angka, tanda hubung, dan garis bawah (maksimal 40 karakter).");
        }
        return trimmed;
    }
}
