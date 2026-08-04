package id.co.bcadigital.credit.cli.remote;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal recursive descent JSON reader. The console application must not depend on external
 * libraries, and the payload of the loan web service is a plain JSON document.
 */
public final class JsonParser {

    private final String source;
    private int position;

    private JsonParser(String source) {
        this.source = source;
    }

    public static Object parse(String json) {
        if (json == null) {
            throw new JsonException("Response kosong.");
        }
        JsonParser parser = new JsonParser(json);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.position != json.length()) {
            throw new JsonException("Karakter tidak dikenal pada posisi " + parser.position + ".");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = parse(json);
        if (!(value instanceof Map)) {
            throw new JsonException("Response bukan JSON object.");
        }
        return (Map<String, Object>) value;
    }

    private Object readValue() {
        if (position >= source.length()) {
            throw new JsonException("Response terpotong.");
        }
        char current = source.charAt(position);
        return switch (current) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        Map<String, Object> values = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return values;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            values.put(key, readValue());
            skipWhitespace();
            char next = peek();
            position++;
            if (next == '}') {
                return values;
            }
            if (next != ',') {
                throw new JsonException("Diharapkan ',' atau '}' pada posisi " + position + ".");
            }
        }
    }

    private List<Object> readArray() {
        List<Object> values = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return values;
        }
        while (true) {
            skipWhitespace();
            values.add(readValue());
            skipWhitespace();
            char next = peek();
            position++;
            if (next == ']') {
                return values;
            }
            if (next != ',') {
                throw new JsonException("Diharapkan ',' atau ']' pada posisi " + position + ".");
            }
        }
    }

    private String readString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (position >= source.length()) {
                throw new JsonException("String JSON tidak ditutup.");
            }
            char current = source.charAt(position++);
            if (current == '"') {
                return builder.toString();
            }
            if (current != '\\') {
                builder.append(current);
                continue;
            }
            char escaped = source.charAt(position++);
            switch (escaped) {
                case '"', '\\', '/' -> builder.append(escaped);
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> {
                    builder.append((char) Integer.parseInt(source.substring(position, position + 4), 16));
                    position += 4;
                }
                default -> throw new JsonException("Escape tidak dikenal: \\" + escaped);
            }
        }
    }

    private BigDecimal readNumber() {
        int start = position;
        while (position < source.length() && "-+.eE0123456789".indexOf(source.charAt(position)) >= 0) {
            position++;
        }
        try {
            return new BigDecimal(source.substring(start, position));
        } catch (NumberFormatException e) {
            throw new JsonException("Angka JSON tidak valid pada posisi " + start + ".");
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!source.startsWith(literal, position)) {
            throw new JsonException("Nilai JSON tidak dikenal pada posisi " + position + ".");
        }
        position += literal.length();
        return value;
    }

    private char peek() {
        if (position >= source.length()) {
            throw new JsonException("Response terpotong.");
        }
        return source.charAt(position);
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw new JsonException("Diharapkan '" + expected + "' pada posisi " + position + ".");
        }
        position++;
    }

    private void skipWhitespace() {
        while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }
}
