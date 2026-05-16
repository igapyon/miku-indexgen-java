package jp.igapyon.mikuindexgen.jsonsummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonSummaryTest {
    @Test
    void parseJsonSummaryPathsParsesAndDeduplicatesACommaSeparatedJsonPointerList() {
        assertEquals(Arrays.asList("/title", "/metadata/name"),
                JsonSummary.parseJsonSummaryPaths("/title, /metadata/name, /title"));
    }

    @Test
    void parseJsonSummaryPathsRequiresJsonPointerSyntax() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> JsonSummary.parseJsonSummaryPaths("title"));
        assertEquals("JSON summary path must be a JSON Pointer starting with \"/\": title", ex.getMessage());
    }

    @Test
    void getJsonPointerValueReadsNestedObjectAndArrayValues() {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("title", "A");
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("items", Arrays.asList(item));

        assertEquals("A", JsonSummary.getJsonPointerValue(root, "/items/0/title"));
    }

    @Test
    void getJsonPointerValueDecodesEscapedPointerSegments() {
        Map<String, Object> inner = new LinkedHashMap<String, Object>();
        inner.put("c~d", "A");
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("a/b", inner);

        assertEquals("A", JsonSummary.getJsonPointerValue(root, "/a~1b/c~0d"));
    }

    @Test
    void extractJsonSummaryUsesTheFirstStringValueFoundByTheConfiguredPaths() {
        List<String> paths = Arrays.asList("/title", "/metadata/title", "/description");

        assertEquals("Data",
                JsonSummary.extractJsonSummary("{\"metadata\":{\"title\":\"Data\"},\"description\":\"Fallback\"}", paths));
    }

    @Test
    void extractJsonSummaryIgnoresInvalidJsonAndNonStringValues() {
        assertEquals("Fallback",
                JsonSummary.extractJsonSummary("{\"title\":123,\"description\":\"Fallback\"}",
                        Arrays.asList("/title", "/description")));
        assertNull(JsonSummary.extractJsonSummary("{", Arrays.asList("/title")));
    }
}
