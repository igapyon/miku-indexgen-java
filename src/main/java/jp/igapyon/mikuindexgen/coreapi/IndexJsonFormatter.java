package jp.igapyon.mikuindexgen.coreapi;

import java.util.List;

import jp.igapyon.mikuindexgen.model.GenerationMetadata;
import jp.igapyon.mikuindexgen.model.IndexFile;
import jp.igapyon.mikuindexgen.model.IndexSource;

final class IndexJsonFormatter {
    private IndexJsonFormatter() {
    }

    static String format(String title, String generator, GenerationMetadata generation, String basePath, List<IndexFile> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        if (title != null) {
            builder.append(" \"title\": ").append(quote(title)).append(",\n");
        }
        if (generator != null) {
            builder.append(" \"generator\": ").append(quote(generator)).append(",\n");
        }
        if (generation != null) {
            builder.append(" \"generation\": ").append(buildGenerationJson(generation)).append(",\n");
        }
        builder.append(" \"basePath\": ").append(quote(basePath)).append(",\n");
        builder.append(" \"files\": ").append(buildFilesJson(files)).append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private static String buildFilesJson(List<IndexFile> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");

        for (int i = 0; i < files.size(); i++) {
            IndexFile file = files.get(i);
            builder.append("  {");
            builder.append(quote("name")).append(":").append(quote(file.name)).append(",");
            builder.append(quote("path")).append(":").append(quote(file.path)).append(",");
            builder.append(quote("ext")).append(":").append(quote(file.ext)).append(",");
            builder.append(quote("dir")).append(":").append(quote(file.dir)).append(",");
            builder.append(quote("size")).append(":").append(file.size);
            appendStringField(builder, "title", file.title);
            appendStringField(builder, "description", file.description);
            appendStringArrayField(builder, "topics", file.topics);
            appendStringField(builder, "category", file.category);
            appendStringField(builder, "status", file.status);
            appendStringArrayField(builder, "audience", file.audience);
            appendStringField(builder, "created", file.created);
            appendStringField(builder, "updated", file.updated);
            if (file.sources != null) {
                builder.append(",").append(quote("sources")).append(":").append(buildSourcesJson(file.sources));
            }
            appendStringField(builder, "summary", file.summary);
            builder.append("}");
            if (i + 1 < files.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }

        builder.append(" ]");
        return builder.toString();
    }

    private static void appendStringField(StringBuilder builder, String name, String value) {
        if (value != null) {
            builder.append(",").append(quote(name)).append(":").append(quote(value));
        }
    }

    private static void appendStringArrayField(StringBuilder builder, String name, List<String> values) {
        if (values != null) {
            builder.append(",").append(quote(name)).append(":").append(buildStringArrayJson(values));
        }
    }

    private static String buildGenerationJson(GenerationMetadata generation) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(quote("schemaVersion")).append(":").append(generation.schemaVersion).append(",");
        builder.append(quote("inputPath")).append(":").append(quote(generation.inputPath)).append(",");
        builder.append(quote("markdownOutput")).append(":").append(generation.markdownOutput).append(",");
        builder.append(quote("recursive")).append(":").append(generation.recursive).append(",");
        builder.append(quote("includeExtensions")).append(":").append(buildStringArrayJson(generation.includeExtensions)).append(",");
        if (generation.excludeGlobs != null && !generation.excludeGlobs.isEmpty()) {
            builder.append(quote("excludeGlobs")).append(":").append(buildStringArrayJson(generation.excludeGlobs))
                    .append(",");
        }
        builder.append(quote("inputEncoding")).append(":").append(quote(generation.inputEncoding)).append(",");
        builder.append(quote("outputEncoding")).append(":").append(quote(generation.outputEncoding));
        if (generation.jsonSummaryPaths != null) {
            builder.append(",").append(quote("jsonSummaryPaths")).append(":")
                    .append(buildStringArrayJson(generation.jsonSummaryPaths));
        }
        appendStringField(builder, "title", generation.title);
        builder.append(",").append(quote("includeGeneratorMetadata")).append(":")
                .append(generation.includeGeneratorMetadata);
        builder.append("}");
        return builder.toString();
    }

    private static String buildSourcesJson(List<IndexSource> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            IndexSource source = sources.get(i);
            builder.append("{");
            builder.append(quote("type")).append(":").append(quote(source.type));
            appendStringField(builder, "role", source.role);
            appendStringField(builder, "label", source.label);
            appendStringField(builder, "url", source.url);
            appendStringField(builder, "path", source.path);
            appendStringField(builder, "version", source.version);
            appendStringField(builder, "checked", source.checked);
            builder.append("}");
        }
        builder.append("]");
        return builder.toString();
    }

    private static String buildStringArrayJson(List<String> values) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(quote(values.get(i)));
        }
        builder.append("]");
        return builder.toString();
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
                    break;
            }
        }
        builder.append('"');
        return builder.toString();
    }
}
