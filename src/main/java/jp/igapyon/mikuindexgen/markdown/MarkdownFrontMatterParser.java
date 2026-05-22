package jp.igapyon.mikuindexgen.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jp.igapyon.mikuindexgen.markdown.Markdown.MarkdownFrontMatter;
import jp.igapyon.mikuindexgen.model.IndexSource;

final class MarkdownFrontMatterParser {
    private static final Pattern DATE_ONLY_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private MarkdownFrontMatterParser() {
    }

    static MarkdownFrontMatter parse(String frontMatter) {
        String[] lines = frontMatter.split("\\r?\\n", -1);
        MarkdownFrontMatter metadata = new MarkdownFrontMatter();

        for (int index = 0; index < lines.length; index++) {
            String rawLine = lines[index];
            String line = rawLine.trim();
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            if (countLeadingSpaces(rawLine) > 0) {
                continue;
            }

            KeyValue keyValue = parseKeyValue(line);
            if (keyValue == null) {
                continue;
            }

            if (containsLikelyInvalidInlineYaml(keyValue.value)) {
                return new MarkdownFrontMatter();
            }

            if ("title".equals(keyValue.key)) {
                metadata.title = sanitizeMetadataString(keyValue.value);
                continue;
            }
            if ("description".equals(keyValue.key)) {
                if (">".equals(keyValue.value)) {
                    FoldedBlock foldedBlock = readFoldedBlock(lines, index + 1);
                    metadata.description = sanitizeMetadataString(foldedBlock.value);
                    index = foldedBlock.lastIndex;
                } else {
                    metadata.description = sanitizeMetadataString(keyValue.value);
                }
                continue;
            }
            if ("category".equals(keyValue.key)) {
                metadata.category = sanitizeMetadataString(keyValue.value);
                continue;
            }
            if ("status".equals(keyValue.key)) {
                metadata.status = sanitizeMetadataString(keyValue.value);
                continue;
            }
            if ("created".equals(keyValue.key)) {
                metadata.created = sanitizeDateOnly(keyValue.value);
                continue;
            }
            if ("updated".equals(keyValue.key)) {
                metadata.updated = sanitizeDateOnly(keyValue.value);
                continue;
            }

            if ("topics".equals(keyValue.key)) {
                ListReadResult result = readStringArrayValue(lines, index, keyValue.value);
                metadata.topics = result.values;
                index = result.lastIndex;
                continue;
            }

            if ("audience".equals(keyValue.key)) {
                ListReadResult result = readStringArrayValue(lines, index, keyValue.value);
                metadata.audience = result.values;
                index = result.lastIndex;
                continue;
            }

            if ("sources".equals(keyValue.key)) {
                SourceReadResult result = readSources(lines, index, keyValue.value);
                metadata.sources = result.sources;
                index = result.lastIndex;
            }
        }

        return metadata;
    }

    private static final class KeyValue {
        private final String key;
        private final String value;

        private KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private static KeyValue parseKeyValue(String line) {
        int colon = line.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        String key = line.substring(0, colon).trim();
        if (!key.matches("[A-Za-z][A-Za-z0-9_-]*")) {
            return null;
        }
        return new KeyValue(key, line.substring(colon + 1).trim());
    }

    private static boolean containsLikelyInvalidInlineYaml(String value) {
        int openSquare = value.indexOf('[');
        int closeSquare = value.indexOf(']');
        return openSquare >= 0 && closeSquare < openSquare;
    }

    private static String sanitizeMetadataString(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        String sanitized = Markdown.sanitizeTextForIndex(unquoteFrontMatterValue(value));
        return sanitized.length() == 0 ? null : sanitized;
    }

    private static String sanitizeDateOnly(String value) {
        String sanitized = sanitizeMetadataString(value);
        return sanitized != null && DATE_ONLY_PATTERN.matcher(sanitized).matches() ? sanitized : null;
    }

    private static final class FoldedBlock {
        private final String value;
        private final int lastIndex;

        private FoldedBlock(String value, int lastIndex) {
            this.value = value;
            this.lastIndex = lastIndex;
        }
    }

    private static FoldedBlock readFoldedBlock(String[] lines, int startIndex) {
        StringBuilder builder = new StringBuilder();
        int lastIndex = startIndex - 1;
        for (int i = startIndex; i < lines.length; i++) {
            String rawLine = lines[i];
            String trimmed = rawLine.trim();
            if (trimmed.length() == 0) {
                lastIndex = i;
                continue;
            }
            if (countLeadingSpaces(rawLine) == 0) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(trimmed);
            lastIndex = i;
        }
        return new FoldedBlock(builder.toString(), lastIndex);
    }

    private static final class ListReadResult {
        private final List<String> values;
        private final int lastIndex;

        private ListReadResult(List<String> values, int lastIndex) {
            this.values = values;
            this.lastIndex = lastIndex;
        }
    }

    private static ListReadResult readStringArrayValue(String[] lines, int keyIndex, String inlineValue) {
        List<String> inlineItems = parseInlineStringArray(inlineValue);
        if (inlineItems != null || inlineValue.length() > 0) {
            return new ListReadResult(inlineItems, keyIndex);
        }

        List<String> items = new ArrayList<String>();
        int lastIndex = keyIndex;
        for (int i = keyIndex + 1; i < lines.length; i++) {
            String rawLine = lines[i];
            String trimmed = rawLine.trim();
            if (trimmed.length() == 0) {
                lastIndex = i;
                continue;
            }
            if (countLeadingSpaces(rawLine) == 0) {
                break;
            }
            if (!trimmed.startsWith("- ")) {
                return new ListReadResult(null, lastIndex);
            }
            String item = trimmed.substring(2).trim();
            if (isUnsupportedPlainScalar(item)) {
                return new ListReadResult(null, i);
            }
            String sanitized = sanitizeMetadataString(item);
            if (sanitized == null) {
                return new ListReadResult(null, i);
            }
            items.add(sanitized);
            lastIndex = i;
        }
        return new ListReadResult(items.isEmpty() ? null : items, lastIndex);
    }

    private static List<String> parseInlineStringArray(String value) {
        String trimmed = value.trim();
        if (trimmed.length() == 0) {
            return null;
        }
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return null;
        }

        List<String> items = new ArrayList<String>();
        String content = trimmed.substring(1, trimmed.length() - 1);
        if (content.trim().length() == 0) {
            return null;
        }
        for (String rawItem : content.split(",")) {
            String item = sanitizeMetadataString(rawItem);
            if (item == null) {
                return null;
            }
            items.add(item);
        }
        return items.isEmpty() ? null : items;
    }

    private static boolean isUnsupportedPlainScalar(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return false;
            }
        }
        return trimmed.indexOf(':') >= 0 || trimmed.startsWith("[") || trimmed.startsWith("{");
    }

    private static final class SourceReadResult {
        private final List<IndexSource> sources;
        private final int lastIndex;

        private SourceReadResult(List<IndexSource> sources, int lastIndex) {
            this.sources = sources;
            this.lastIndex = lastIndex;
        }
    }

    private static SourceReadResult readSources(String[] lines, int keyIndex, String inlineValue) {
        if (inlineValue.length() > 0) {
            return new SourceReadResult(null, keyIndex);
        }
        List<IndexSource> sources = new ArrayList<IndexSource>();
        IndexSource current = null;
        boolean invalid = false;
        int lastIndex = keyIndex;
        for (int i = keyIndex + 1; i < lines.length; i++) {
            String rawLine = lines[i];
            String trimmed = rawLine.trim();
            if (trimmed.length() == 0) {
                lastIndex = i;
                continue;
            }
            if (countLeadingSpaces(rawLine) == 0) {
                break;
            }
            if (trimmed.startsWith("- ")) {
                current = new IndexSource();
                sources.add(current);
                String rest = trimmed.substring(2).trim();
                if (rest.length() > 0) {
                    invalid |= !applySourceField(current, rest);
                }
                lastIndex = i;
                continue;
            }
            if (current == null) {
                invalid = true;
                lastIndex = i;
                continue;
            }
            invalid |= !applySourceField(current, trimmed);
            lastIndex = i;
        }
        for (IndexSource source : sources) {
            if (source.type == null) {
                invalid = true;
            }
        }
        return new SourceReadResult(invalid || sources.isEmpty() ? null : sources, lastIndex);
    }

    private static boolean applySourceField(IndexSource source, String line) {
        KeyValue keyValue = parseKeyValue(line);
        if (keyValue == null) {
            return false;
        }
        String value = "checked".equals(keyValue.key) ? sanitizeDateOnly(keyValue.value) : sanitizeMetadataString(keyValue.value);
        if (value == null) {
            return false;
        }
        if ("type".equals(keyValue.key)) {
            source.type = value;
            return true;
        }
        if ("role".equals(keyValue.key)) {
            source.role = value;
            return true;
        }
        if ("label".equals(keyValue.key)) {
            source.label = value;
            return true;
        }
        if ("url".equals(keyValue.key)) {
            source.url = value;
            return true;
        }
        if ("path".equals(keyValue.key)) {
            source.path = value;
            return true;
        }
        if ("version".equals(keyValue.key)) {
            source.version = value;
            return true;
        }
        if ("checked".equals(keyValue.key)) {
            source.checked = value;
            return true;
        }
        return true;
    }

    private static int countLeadingSpaces(String value) {
        int count = 0;
        while (count < value.length() && value.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private static String unquoteFrontMatterValue(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }
}
