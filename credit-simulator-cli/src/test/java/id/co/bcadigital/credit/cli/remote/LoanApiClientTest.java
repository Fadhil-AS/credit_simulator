package id.co.bcadigital.credit.cli.remote;

import com.sun.net.httpserver.HttpServer;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleType;
import id.co.bcadigital.credit.core.validation.LoanInputParser;
import id.co.bcadigital.credit.core.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanApiClientTest {

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
    void shouldMapWebServicePayloadToLoanApplication() {
        respond(200, """
                {
                  "vehicleType": "Mobil",
                  "vehicleCondition": "Baru",
                  "vehicleYear": 2025,
                  "totalLoanAmount": 1000000000,
                  "loanTenure": 6,
                  "downPayment": 500000000
                }
                """);

        LoanApplication application = client(2025).fetchExistingCalculation();

        assertEquals(VehicleType.MOBIL, application.vehicle().type());
        assertEquals(2025, application.vehicle().year());
        assertEquals(6, application.tenureYears());
        assertEquals(0, new BigDecimal("500000000").compareTo(application.downPayment()));
    }

    @Test
    void shouldRejectPayloadViolatingBusinessRules() {
        respond(200, """
                {
                  "vehicleType": "Mobil",
                  "vehicleCondition": "Baru",
                  "vehicleYear": 2025,
                  "totalLoanAmount": 1000000000,
                  "loanTenure": 6,
                  "downPayment": 100000000
                }
                """);

        assertThrows(ValidationException.class, () -> client(2025).fetchExistingCalculation());
    }

    @Test
    void shouldReportMissingField() {
        respond(200, "{\"vehicleType\": \"Mobil\"}");

        assertThrows(RemoteServiceException.class, () -> client(2025).fetchExistingCalculation());
    }

    @Test
    void shouldReportHttpErrorWithResponseBody() {
        respond(404, """
                {
                  "status_code": 404,
                  "route": "GET run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c"
                }
                """);

        RemoteServiceException failure = assertThrows(RemoteServiceException.class,
                () -> client(2025).fetchExistingCalculation());

        assertTrue(failure.getMessage().contains("HTTP 404"), failure.getMessage());
        assertTrue(failure.getMessage().contains("\"status_code\": 404"), failure.getMessage());
    }

    @Test
    void shouldTruncateVeryLongErrorBody() {
        respond(503, "x".repeat(2000));

        RemoteServiceException failure = assertThrows(RemoteServiceException.class,
                () -> client(2025).fetchExistingCalculation());

        assertTrue(failure.getMessage().endsWith("..."), failure.getMessage());
        assertTrue(failure.getMessage().length() < 400, "pesan terlalu panjang: " + failure.getMessage().length());
    }

    private LoanApiClient client(int currentYear) {
        return new LoanApiClient(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/loan"),
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/fallback"),
                new LoanInputParser(currentYear));
    }

    private void respond(int status, String body) {
        server.createContext("/loan", exchange -> {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        });
    }
}
