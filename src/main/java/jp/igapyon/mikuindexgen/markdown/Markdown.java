package jp.igapyon.mikuindexgen.markdown;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import jp.igapyon.mikuindexgen.model.IndexFile;
import jp.igapyon.mikuindexgen.model.IndexSource;

public final class Markdown {
    private Markdown() {
    }

    public static final class MarkdownFrontMatter {
        public String title;
        public String description;
        public List<String> topics;
        public String category;
        public String status;
        public List<String> audience;
        public String created;
        public String updated;
        public List<IndexSource> sources;
    }

    public static final class MarkdownFrontMatterResult {
        public String body;
        public MarkdownFrontMatter metadata;

        private MarkdownFrontMatterResult(String body, MarkdownFrontMatter metadata) {
            this.body = body;
            this.metadata = metadata;
        }
    }

    public static String sanitizeTextForIndex(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if ((ch >= 0x0000 && ch <= 0x0008)
                    || (ch >= 0x000B && ch <= 0x001F)
                    || (ch >= 0x007F && ch <= 0x009F)
                    || (ch >= 0x200B && ch <= 0x200D)
                    || ch == 0x2060
                    || ch == 0xFEFF
                    || ch == '\r'
                    || ch == '\n'
                    || ch == '\t') {
                builder.append(' ');
            } else {
                builder.append(ch);
            }
        }

        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    public static String truncateTextForIndex(String text, int maxLength) {
        return truncateTextForIndex(text, maxLength, "...");
    }

    public static String truncateTextForIndex(String text, int maxLength, String omission) {
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= omission.length()) {
            return omission.substring(0, maxLength);
        }
        return text.substring(0, maxLength - omission.length()) + omission;
    }

    public static String escapeMarkdownTableCell(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .trim();
    }

    public static String extractSummary(String markdown) {
        return extractSummary(markdown, 256);
    }

    public static String extractSummary(String markdown, int maxLength) {
        return extractSummaryFromBody(extractFrontMatter(markdown).body, maxLength);
    }

    public static String extractSummaryFromBody(String markdownBody) {
        return extractSummaryFromBody(markdownBody, 256);
    }

    public static String extractSummaryFromBody(String markdownBody, int maxLength) {
        String[] lines = markdownBody.split("\\r?\\n", -1);
        String firstNonEmptyLine = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.length() == 0) {
                continue;
            }
            firstNonEmptyLine = line;
            break;
        }

        if (firstNonEmptyLine == null) {
            return null;
        }

        if (firstNonEmptyLine.startsWith("#")) {
            String heading = firstNonEmptyLine.replaceFirst("^#+\\s*", "").trim();
            if (heading.length() == 0) {
                return null;
            }
            return sanitizeTextForIndex(heading);
        }

        StringBuilder body = new StringBuilder();
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("#")) {
                break;
            }
            if (body.length() > 0) {
                body.append(' ');
            }
            body.append(line);
            if (body.length() >= maxLength) {
                return sanitizeTextForIndex(body.substring(0, maxLength));
            }
        }

        if (body.length() == 0) {
            return null;
        }

        String summary = body.length() > maxLength ? body.substring(0, maxLength) : body.toString();
        return sanitizeTextForIndex(summary);
    }

    public static MarkdownFrontMatterResult extractFrontMatter(String markdown) {
        String normalizedMarkdown = markdown.startsWith("\uFEFF") ? markdown.substring(1) : markdown;
        int firstLineEnd = findFirstLineEnd(normalizedMarkdown);
        String firstLine = firstLineEnd == -1 ? normalizedMarkdown : normalizedMarkdown.substring(0, firstLineEnd);

        if (!"---".equals(firstLine.trim())) {
            return frontMatterResult(markdown, new MarkdownFrontMatter());
        }

        int contentStart = firstLineEnd == -1 ? normalizedMarkdown.length()
                : firstLineEnd + lineSeparatorLength(normalizedMarkdown, firstLineEnd);
        ClosingDelimiter closing = findClosingDelimiter(normalizedMarkdown, contentStart);
        if (closing == null) {
            return frontMatterResult(markdown, new MarkdownFrontMatter());
        }

        String frontMatter = normalizedMarkdown.substring(contentStart, closing.start);
        int bodyStart = closing.end;
        if (bodyStart < normalizedMarkdown.length()) {
            if (normalizedMarkdown.charAt(bodyStart) == '\r'
                    && bodyStart + 1 < normalizedMarkdown.length()
                    && normalizedMarkdown.charAt(bodyStart + 1) == '\n') {
                bodyStart += 2;
            } else if (normalizedMarkdown.charAt(bodyStart) == '\n') {
                bodyStart += 1;
            }
        }

        return frontMatterResult(stripLeadingLineBreaks(normalizedMarkdown.substring(bodyStart)),
                MarkdownFrontMatterParser.parse(frontMatter));
    }

    private static String stripLeadingLineBreaks(String value) {
        int index = 0;
        while (index < value.length()) {
            char ch = value.charAt(index);
            if (ch == '\r') {
                index++;
                if (index < value.length() && value.charAt(index) == '\n') {
                    index++;
                }
                continue;
            }
            if (ch == '\n') {
                index++;
                continue;
            }
            break;
        }
        return value.substring(index);
    }

    private static MarkdownFrontMatterResult frontMatterResult(String body, MarkdownFrontMatter metadata) {
        return new MarkdownFrontMatterResult(body, metadata);
    }

    private static int findFirstLineEnd(String text) {
        int cr = text.indexOf('\r');
        int lf = text.indexOf('\n');
        if (cr == -1) {
            return lf;
        }
        if (lf == -1) {
            return cr;
        }
        return Math.min(cr, lf);
    }

    private static int lineSeparatorLength(String text, int lineEnd) {
        return text.charAt(lineEnd) == '\r'
                && lineEnd + 1 < text.length()
                && text.charAt(lineEnd + 1) == '\n' ? 2 : 1;
    }

    private static final class ClosingDelimiter {
        private final int start;
        private final int end;

        private ClosingDelimiter(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static ClosingDelimiter findClosingDelimiter(String markdown, int contentStart) {
        int lineStart = contentStart;
        while (lineStart <= markdown.length()) {
            int lineEnd = findLineEnd(markdown, lineStart);
            String line = markdown.substring(lineStart, lineEnd);
            if ("---".equals(line.trim())) {
                return new ClosingDelimiter(lineStart, lineEnd);
            }
            if (lineEnd >= markdown.length()) {
                break;
            }
            lineStart = lineEnd + lineSeparatorLength(markdown, lineEnd);
        }
        return null;
    }

    private static int findLineEnd(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\r' || ch == '\n') {
                return i;
            }
        }
        return text.length();
    }

    public static String buildMarkdownIndexContent(List<IndexFile> files) {
        List<String> lines = new ArrayList<String>();
        lines.add("# Index");
        lines.add("");

        if (files.isEmpty()) {
            lines.add("No matching files found.");
            lines.add("");
            return joinLines(lines);
        }

        lines.add("| File | Ext | Dir | Size | Summary |");
        lines.add("| --- | --- | --- | ---: | --- |");

        for (IndexFile file : files) {
            String fileLabel = escapeMarkdownTableCell(file.path);
            String ext = escapeMarkdownTableCell(file.ext);
            String dir = escapeMarkdownTableCell(file.dir);
            String summary = escapeMarkdownTableCell(file.summary == null ? "" : file.summary);
            lines.add("| [" + fileLabel + "](" + file.path + ") | " + ext + " | " + dir + " | " + file.size + " | " + summary + " |");
        }

        lines.add("");
        return joinLines(lines);
    }

    private static String joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(lines.get(i));
        }
        return builder.toString();
    }
}
