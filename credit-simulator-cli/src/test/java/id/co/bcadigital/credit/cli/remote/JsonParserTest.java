package id.co.bcadigital.credit.cli.remote;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonParserTest {

    @Test
    void shouldParseTheLoanPayload() {
        Map<String, Object> payload = JsonParser.parseObject("""
                {
                  "vehicleType": "Mobil",
                  "vehicleCondition": "Baru",
                  "vehicleYear": 2025,
                  "totalLoanAmount": 1000000000,
                  "loanTenure": 6,
                  "downPayment": 500000000
                }
                """);

        assertEquals("Mobil", payload.get("vehicleType"));
        assertEquals(0, new BigDecimal("2025").compareTo((BigDecimal) payload.get("vehicleYear")));
        assertEquals(0, new BigDecimal("500000000").compareTo((BigDecimal) payload.get("downPayment")));
    }

    @Test
    void shouldParseNestedStructuresAndEscapes() {
        Map<String, Object> payload = JsonParser.parseObject(
                "{\"items\":[1,2.5,true,false,null],\"text\":\"baris\\nbaru \\u00e9\",\"nested\":{\"a\":\"b\"}}");

        List<?> items = (List<?>) payload.get("items");
        assertEquals(5, items.size());
        assertEquals(0, new BigDecimal("2.5").compareTo((BigDecimal) items.get(1)));
        assertEquals(Boolean.TRUE, items.get(2));
        assertNull(items.get(4));
        assertTrue(((String) payload.get("text")).contains("\n"));
        assertTrue(((String) payload.get("text")).endsWith("é"));
        assertEquals(Map.of("a", "b"), payload.get("nested"));
    }

    @Test
    void shouldParseEmptyContainers() {
        assertEquals(Map.of(), JsonParser.parseObject("{}"));
        assertEquals(List.of(), JsonParser.parse("[]"));
        assertNull(JsonParser.parse("null"));
    }

    @Test
    void shouldRejectMalformedDocuments() {
        assertThrows(JsonException.class, () -> JsonParser.parseObject("{\"a\":}"));
        assertThrows(JsonException.class, () -> JsonParser.parseObject("{\"a\":1"));
        assertThrows(JsonException.class, () -> JsonParser.parseObject("[1,2]"));
        assertThrows(JsonException.class, () -> JsonParser.parseObject("{\"a\":1} trailing"));
        assertThrows(JsonException.class, () -> JsonParser.parseObject(null));
    }
}
