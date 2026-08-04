package id.co.bcadigital.credit.cli.remote;

import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;
import id.co.bcadigital.credit.core.validation.LoanInputParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

public class LoanApiClient {

    public static final String DEFAULT_ENDPOINT = "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c";
    public static final String DEFAULT_FALLBACK_ENDPOINT = "http://localhost:8000/loan.json";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_ERROR_BODY_LENGTH = 300;
    private static final Pattern PAYLOAD_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,39}");

    private final HttpClient httpClient;
    private final URI endpoint;
    private final URI fallbackEndpoint;
    private final LoanInputParser parser;

    public LoanApiClient(URI endpoint, URI fallbackEndpoint, LoanInputParser parser) {
        this.endpoint = endpoint;
        this.fallbackEndpoint = fallbackEndpoint;
        this.parser = parser;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static LoanApiClient withDefaultEndpoint(LoanInputParser parser) {
        Map<String, String> environment = System.getenv();
        return new LoanApiClient(
                URI.create(environment.getOrDefault("CREDIT_SIMULATOR_LOAN_API", DEFAULT_ENDPOINT)),
                URI.create(environment.getOrDefault("CREDIT_SIMULATOR_LOAN_API_FALLBACK", DEFAULT_FALLBACK_ENDPOINT)),
                parser);
    }

    public URI endpoint() {
        return endpoint;
    }

    public URI fallbackEndpoint() {
        return fallbackEndpoint;
    }

    public URI resolvePayload(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (!PAYLOAD_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Nama payload hanya boleh huruf, angka, tanda hubung, dan garis bawah (maksimal 40 karakter).");
        }
        return fallbackEndpoint.resolve(trimmed + ".json");
    }

    public LoanApplication fetchExistingCalculation() {
        return fetchFrom(endpoint);
    }

    public LoanApplication fetchFrom(URI target) {
        HttpRequest request = HttpRequest.newBuilder(target)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RemoteServiceException("Gagal menghubungi web service: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteServiceException("Permintaan ke web service dibatalkan.", e);
        }
        if (response.statusCode() != 200) {
            throw new RemoteServiceException("Web service membalas HTTP " + response.statusCode()
                    + ". Response: " + summarize(response.body()));
        }
        return toApplication(JsonParser.parseObject(response.body()));
    }

    private static String summarize(String body) {
        if (body == null || body.isBlank()) {
            return "(kosong)";
        }
        String compacted = body.strip().replaceAll("\\s+", " ");
        return compacted.length() <= MAX_ERROR_BODY_LENGTH
                ? compacted
                : compacted.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }

    private LoanApplication toApplication(Map<String, Object> payload) {
        VehicleType type = parser.parseVehicleType(text(payload, "vehicleType"));
        VehicleCondition condition = parser.parseVehicleCondition(text(payload, "vehicleCondition"));
        int year = parser.validateVehicleYear(number(payload, "vehicleYear").intValueExact(), condition);
        BigDecimal loanAmount = parser.validateLoanAmount(number(payload, "totalLoanAmount"));
        int tenure = parser.validateTenure(number(payload, "loanTenure").intValueExact());
        BigDecimal downPayment = parser.validateDownPayment(number(payload, "downPayment"), loanAmount, condition);
        return new LoanApplication(VehicleFactory.create(type, condition, year), loanAmount, tenure, downPayment);
    }

    private static String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (!(value instanceof String text)) {
            throw new RemoteServiceException("Field '" + key + "' tidak ditemukan pada response web service.");
        }
        return text;
    }

    private static BigDecimal number(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (!(value instanceof BigDecimal amount)) {
            throw new RemoteServiceException("Field '" + key + "' harus berupa angka pada response web service.");
        }
        return amount;
    }
}
