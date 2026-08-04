package id.co.bcadigital.credit.cli.presenter;

import com.sun.net.httpserver.HttpServer;
import id.co.bcadigital.credit.cli.CreditSimulatorApplication;
import id.co.bcadigital.credit.cli.io.ScriptedLineReader;
import id.co.bcadigital.credit.cli.remote.LoanApiClient;
import id.co.bcadigital.credit.cli.sheet.PayloadRepository;
import id.co.bcadigital.credit.cli.sheet.SheetRepository;
import id.co.bcadigital.credit.cli.view.SystemConsoleView;
import id.co.bcadigital.credit.core.calculation.DecliningBalanceInstallmentCalculator;
import id.co.bcadigital.credit.core.validation.LoanInputParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadFailoverTest {

    private static final String PAYLOAD = """
            {
              "vehicleType": "Mobil",
              "vehicleCondition": "Bekas",
              "vehicleYear": 2020,
              "totalLoanAmount": 100000000,
              "loanTenure": 3,
              "downPayment": 25000000
            }
            """;

    @TempDir
    Path sheetDirectory;

    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("endpoint utama mati, hasil diambil dari endpoint cadangan")
    void shouldFallBackWhenPrimaryEndpointFails() {
        serve("/fallback", 200, PAYLOAD);

        String output = load(URI.create("http://127.0.0.1:1/mati"), fallbackUri());

        assertTrue(output.contains("Mencoba endpoint cadangan"), output);
        assertTrue(output.contains("tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%"), output);
    }

    @Test
    @DisplayName("endpoint utama hidup, endpoint cadangan tidak disentuh")
    void shouldNotFallBackWhenPrimaryEndpointWorks() {
        serve("/loan", 200, PAYLOAD);

        String output = load(primaryUri(), URI.create("http://127.0.0.1:1/tidak-dipakai"));

        assertFalse(output.contains("Mencoba endpoint cadangan"), output);
        assertTrue(output.contains("tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"), output);
    }

    @Test
    @DisplayName("kedua endpoint gagal, error dilaporkan dan aplikasi tetap hidup")
    void shouldReportErrorWhenBothEndpointsFail() {
        String output = load(URI.create("http://127.0.0.1:1/mati"), URI.create("http://127.0.0.1:1/mati-juga"));

        assertTrue(output.contains("Mencoba endpoint cadangan"), output);
        assertTrue(output.contains("Gagal menghubungi web service"), output);
        assertTrue(output.contains("Terima kasih telah menggunakan Credit Simulator."), output);
    }

    private URI primaryUri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/loan");
    }

    private URI fallbackUri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/fallback");
    }

    private String load(URI primary, URI fallback) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        SystemConsoleView view = new SystemConsoleView(out, new ScriptedLineReader(List.of("load", "exit"), out));
        LoanInputParser parser = new LoanInputParser();
        SimulationPresenter presenter = new SimulationPresenter(
                view,
                new DecliningBalanceInstallmentCalculator(),
                parser,
                new SheetRepository(sheetDirectory),
                new PayloadRepository(sheetDirectory.resolve("mock")),
                new LoanApiClient(primary, fallback, parser));
        CreditSimulatorApplication.newController(view, presenter).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void serve(String path, int status, String body) {
        server.createContext(path, exchange -> {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(payload);
            }
        });
    }
}
