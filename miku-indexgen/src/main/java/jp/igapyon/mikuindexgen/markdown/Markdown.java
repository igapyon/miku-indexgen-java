package jp.igapyon.mikuindexgen.markdown;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import jp.igapyon.mikuindexgen.model.IndexFile;

public final class Markdown {
    private Markdown() {
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
        String[] lines = markdown.split("\\r?\\n", -1);
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
