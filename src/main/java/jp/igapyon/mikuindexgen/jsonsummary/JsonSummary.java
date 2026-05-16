package jp.igapyon.mikuindexgen.jsonsummary;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jp.igapyon.mikuindexgen.markdown.Markdown;

public final class JsonSummary {
    private static final int DEFAULT_MAX_SUMMARY_LENGTH = 256;

    private JsonSummary() {
    }

    public static List<String> parseJsonSummaryPaths(String value) {
        String[] rawItems = value.split(",");
        LinkedHashSet<String> paths = new LinkedHashSet<String>();

        for (String rawItem : rawItems) {
            String path = rawItem.trim();
            if (path.length() == 0) {
                continue;
            }
            if (!path.startsWith("/")) {
                throw new IllegalArgumentException("JSON summary path must be a JSON Pointer starting with \"/\": " + path);
            }
            paths.add(path);
        }

        if (paths.isEmpty()) {
            throw new IllegalArgumentException("Please specify at least one JSON Pointer for --json-summary-path.");
        }

        return new ArrayList<String>(paths);
    }

    public static Object getJsonPointerValue(Object value, String pointer) {
        Object current = value;
        String[] segments = pointer.substring(1).split("/", -1);

        for (String rawSegment : segments) {
            String segment = decodeJsonPointerSegment(rawSegment);
            if (current == null) {
                return null;
            }

            if (current instanceof List) {
                if (!segment.matches("^(0|[1-9]\\d*)$")) {
                    return null;
                }
                List<?> list = (List<?>) current;
                int index = Integer.parseInt(segment);
                if (index < 0 || index >= list.size()) {
                    return null;
                }
                current = list.get(index);
                continue;
            }

            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(segment);
                continue;
            }

            return null;
        }

        return current;
    }

    public static String extractJsonSummary(String jsonText, List<String> paths) {
        return extractJsonSummary(jsonText, paths, DEFAULT_MAX_SUMMARY_LENGTH);
    }

    public static String extractJsonSummary(String jsonText, List<String> paths, int maxLength) {
        Object parsed;
        try {
            parsed = new Parser(jsonText).parse();
        } catch (RuntimeException ex) {
            return null;
        }

        for (String path : paths) {
            Object value = getJsonPointerValue(parsed, path);
            if (!(value instanceof String)) {
                continue;
            }

            String stringValue = (String) value;
            String sliced = stringValue.length() > maxLength ? stringValue.substring(0, maxLength) : stringValue;
            String summary = Markdown.sanitizeTextForIndex(sliced);
            if (summary.length() > 0) {
                return summary;
            }
        }

        return null;
    }

    private static String decodeJsonPointerSegment(String segment) {
        return segment.replace("~1", "/").replace("~0", "~");
    }
}
