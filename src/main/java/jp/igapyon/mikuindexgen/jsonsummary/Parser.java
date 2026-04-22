package jp.igapyon.mikuindexgen.jsonsummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Parser {
    private final String text;
    private int index;

    Parser(String text) {
        this.text = text;
    }

    Object parse() {
        Object value = parseValue();
        skipWhitespace();
        if (index != text.length()) {
            throw new IllegalArgumentException("Unexpected trailing JSON content.");
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (index >= text.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON content.");
        }

        char ch = text.charAt(index);
        if (ch == '{') {
            return parseObject();
        }
        if (ch == '[') {
            return parseArray();
        }
        if (ch == '"') {
            return parseString();
        }
        if (ch == 't') {
            readLiteral("true");
            return Boolean.TRUE;
        }
        if (ch == 'f') {
            readLiteral("false");
            return Boolean.FALSE;
        }
        if (ch == 'n') {
            readLiteral("null");
            return null;
        }
        if (ch == '-' || (ch >= '0' && ch <= '9')) {
            return parseNumber();
        }

        throw new IllegalArgumentException("Unexpected JSON value.");
    }

    private Map<String, Object> parseObject() {
        expect('{');
        LinkedHashMap<String, Object> object = new LinkedHashMap<String, Object>();
        skipWhitespace();
        if (peek('}')) {
            expect('}');
            return object;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            object.put(key, value);
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return object;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        expect('[');
        ArrayList<Object> array = new ArrayList<Object>();
        skipWhitespace();
        if (peek(']')) {
            expect(']');
            return array;
        }

        while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return array;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();

        while (index < text.length()) {
            char ch = text.charAt(index++);
            if (ch == '"') {
                return builder.toString();
            }
            if (ch != '\\') {
                builder.append(ch);
                continue;
            }
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON escape.");
            }
            char escaped = text.charAt(index++);
            switch (escaped) {
                case '"':
                case '\\':
                case '/':
                    builder.append(escaped);
                    break;
                case 'b':
                    builder.append('\b');
                    break;
                case 'f':
                    builder.append('\f');
                    break;
                case 'n':
                    builder.append('\n');
                    break;
                case 'r':
                    builder.append('\r');
                    break;
                case 't':
                    builder.append('\t');
                    break;
                case 'u':
                    builder.append(parseUnicodeEscape());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported JSON escape.");
            }
        }

        throw new IllegalArgumentException("Unterminated JSON string.");
    }

    private char parseUnicodeEscape() {
        if (index + 4 > text.length()) {
            throw new IllegalArgumentException("Invalid JSON unicode escape.");
        }
        String hex = text.substring(index, index + 4);
        index += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private Number parseNumber() {
        int start = index;
        if (peek('-')) {
            index++;
        }
        while (index < text.length() && Character.isDigit(text.charAt(index))) {
            index++;
        }
        if (peek('.')) {
            index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
        }

        return Double.valueOf(text.substring(start, index));
    }

    private void readLiteral(String literal) {
        if (!text.startsWith(literal, index)) {
            throw new IllegalArgumentException("Unexpected JSON literal.");
        }
        index += literal.length();
    }

    private boolean peek(char expected) {
        return index < text.length() && text.charAt(index) == expected;
    }

    private void expect(char expected) {
        if (!peek(expected)) {
            throw new IllegalArgumentException("Expected JSON character: " + expected);
        }
        index++;
    }

    private void skipWhitespace() {
        while (index < text.length()) {
            char ch = text.charAt(index);
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                index++;
                continue;
            }
            return;
        }
    }
}
