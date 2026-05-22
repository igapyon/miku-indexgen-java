package jp.igapyon.mikuindexgen.coreapi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.igapyon.mikuindexgen.encoding.Encoding;
import jp.igapyon.mikuindexgen.json.JsonParser;
import jp.igapyon.mikuindexgen.model.GenerationMetadata;
import jp.igapyon.mikuindexgen.pathutils.PathUtils;

public final class Generation {
    private Generation() {
    }

    public static GenerationMetadata buildGenerationMetadata(IndexgenOptions options, Path targetPath, Path outputPath) {
        GenerationMetadata generation = new GenerationMetadata();
        Path parent = outputPath.getParent();
        String inputPath = parent == null ? targetPath.toString() : parent.relativize(targetPath).toString();
        generation.inputPath = normalizeEmptyPath(PathUtils.toPosixPath(inputPath));
        generation.markdownOutput = options.markdownOutput;
        generation.recursive = options.recursive;
        generation.includeExtensions = copyList(options.includeExtensions);
        generation.inputEncoding = options.inputEncoding;
        generation.outputEncoding = options.outputEncoding;
        if (options.jsonSummaryPaths != null && !options.jsonSummaryPaths.isEmpty()) {
            generation.jsonSummaryPaths = copyList(options.jsonSummaryPaths);
        }
        generation.title = options.title;
        generation.includeGeneratorMetadata = !Boolean.FALSE.equals(options.includeGeneratorMetadata);
        return generation;
    }

    public static GenerationMetadata readGenerationMetadata(Path indexPath) throws IOException {
        String content = Encoding.readTextFile(indexPath, "utf8");
        Object parsed;
        try {
            parsed = new JsonParser(content).parse();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Could not parse refresh index JSON: " + ex.getMessage());
        }

        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Refresh index does not include generation metadata. Regenerate it with --input-directory first.");
        }
        Object generationValue = ((Map<?, ?>) parsed).get("generation");
        if (!(generationValue instanceof Map)) {
            throw new IllegalArgumentException("Refresh index does not include generation metadata. Regenerate it with --input-directory first.");
        }
        return toGenerationMetadata((Map<?, ?>) generationValue);
    }

    public static IndexgenOptions buildRefreshOptions(IndexgenOptions options, Path indexPath) throws IOException {
        indexPath = indexPath.toAbsolutePath().normalize();
        GenerationMetadata generation = readGenerationMetadata(indexPath);
        Path outputDirectory = indexPath.getParent();
        Path inputDirectory = outputDirectory.resolve(generation.inputPath).toAbsolutePath().normalize();

        IndexgenOptions refreshOptions = new IndexgenOptions();
        refreshOptions.inputDirectory = inputDirectory.toString();
        refreshOptions.outputDirectory = outputDirectory.toString();
        refreshOptions.title = generation.title;
        refreshOptions.markdownOutput = generation.markdownOutput;
        refreshOptions.includeGeneratorMetadata = Boolean.valueOf(generation.includeGeneratorMetadata);
        refreshOptions.jsonSummaryPaths = copyList(generation.jsonSummaryPaths);
        refreshOptions.recursive = generation.recursive;
        refreshOptions.overwrite = options.overwrite;
        refreshOptions.verbose = options.verbose;
        refreshOptions.verboseStream = options.verboseStream;
        refreshOptions.includeExtensions = copyList(generation.includeExtensions);
        refreshOptions.inputEncoding = generation.inputEncoding;
        refreshOptions.outputEncoding = generation.outputEncoding;
        return refreshOptions;
    }

    private static GenerationMetadata toGenerationMetadata(Map<?, ?> value) {
        GenerationMetadata generation = new GenerationMetadata();
        generation.schemaVersion = intValue(value.get("schemaVersion"), "schemaVersion");
        if (generation.schemaVersion != 1) {
            throw invalidGeneration();
        }
        generation.inputPath = stringValue(value.get("inputPath"), "inputPath");
        generation.markdownOutput = booleanValue(value.get("markdownOutput"), "markdownOutput");
        generation.recursive = booleanValue(value.get("recursive"), "recursive");
        generation.includeExtensions = stringArrayValue(value.get("includeExtensions"), "includeExtensions");
        generation.inputEncoding = stringValue(value.get("inputEncoding"), "inputEncoding");
        generation.outputEncoding = stringValue(value.get("outputEncoding"), "outputEncoding");
        if (value.containsKey("jsonSummaryPaths")) {
            generation.jsonSummaryPaths = stringArrayValue(value.get("jsonSummaryPaths"), "jsonSummaryPaths");
        }
        if (value.containsKey("title")) {
            generation.title = stringValue(value.get("title"), "title");
        }
        generation.includeGeneratorMetadata = booleanValue(value.get("includeGeneratorMetadata"), "includeGeneratorMetadata");
        return generation;
    }

    private static int intValue(Object value, String name) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw invalidGeneration(name);
    }

    private static String stringValue(Object value, String name) {
        if (value instanceof String) {
            return (String) value;
        }
        throw invalidGeneration(name);
    }

    private static boolean booleanValue(Object value, String name) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        throw invalidGeneration(name);
    }

    private static List<String> stringArrayValue(Object value, String name) {
        if (!(value instanceof List)) {
            throw invalidGeneration(name);
        }
        List<?> rawItems = (List<?>) value;
        List<String> items = new ArrayList<String>();
        for (Object item : rawItems) {
            if (!(item instanceof String)) {
                throw invalidGeneration(name);
            }
            items.add((String) item);
        }
        return items;
    }

    private static IllegalArgumentException invalidGeneration() {
        return new IllegalArgumentException("Refresh index includes invalid generation metadata. Regenerate it with --input-directory first.");
    }

    private static IllegalArgumentException invalidGeneration(String name) {
        return new IllegalArgumentException("Refresh index includes invalid generation metadata field: " + name + ".");
    }

    private static String normalizeEmptyPath(String value) {
        return value == null || value.length() == 0 ? "." : value;
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }
}
