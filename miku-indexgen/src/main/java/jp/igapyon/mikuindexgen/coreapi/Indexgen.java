package jp.igapyon.mikuindexgen.coreapi;

import java.io.IOException;
import java.text.Collator;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jp.igapyon.mikuindexgen.encoding.Encoding;
import jp.igapyon.mikuindexgen.jsonsummary.JsonSummary;
import jp.igapyon.mikuindexgen.logging.Logging;
import jp.igapyon.mikuindexgen.logging.VerboseLogger;
import jp.igapyon.mikuindexgen.markdown.Markdown;
import jp.igapyon.mikuindexgen.model.IndexFile;
import jp.igapyon.mikuindexgen.pathutils.PathUtils;

public class Indexgen {
    private static final String GENERATOR_NAME = "miku-indexgen";
    private static final String MARKDOWN_OUTPUT_FILE_NAME = "index.md";
    private static final Collator JAPANESE_COLLATOR = Collator.getInstance(Locale.JAPANESE);

    public List<Path> collectIndexableFiles(Path dirPath, boolean recursive, List<String> includeExtensions) throws IOException {
        Set<String> allowedExtensions = new LinkedHashSet<String>(includeExtensions);
        return collectIndexableFilesWithSet(dirPath, recursive, allowedExtensions, listVisibleEntries(dirPath));
    }

    public String buildIndexContent(String title, Path targetPath, List<IndexFile> files, Path outputPath,
            boolean includeGeneratorMetadata) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        List<String> fields = new ArrayList<String>();
        if (title != null) {
            fields.add("  \"title\": " + quote(title));
        }
        if (includeGeneratorMetadata) {
            fields.add("  \"generator\": " + quote(GENERATOR_NAME));
        }
        String basePath = PathUtils.toPosixPath(outputPath.getParent().relativize(targetPath).toString());
        if (basePath.length() == 0) {
            basePath = ".";
        }
        fields.add("  \"basePath\": " + quote(basePath));
        fields.add("  \"files\": " + buildFilesJson(files));

        for (int i = 0; i < fields.size(); i++) {
            builder.append(fields.get(i));
            if (i + 1 < fields.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }

        builder.append("}\n");
        return builder.toString();
    }

    public IndexgenResult createIndexes(IndexgenOptions options) throws IOException {
        long totalStart = System.nanoTime();
        Path targetPath = Paths.get(options.targetDir).toAbsolutePath().normalize();

        if (!Files.isDirectory(targetPath)) {
            throw new IllegalArgumentException("Target directory does not exist: " + targetPath);
        }

        IndexgenResult result = new IndexgenResult();
        OutputPaths outputPaths = getOutputPaths(targetPath, options);
        result.jsonPath = outputPaths.jsonPath;
        result.markdownPath = outputPaths.markdownPath;

        VerboseLogger logger = new VerboseLogger(options.verbose);
        Logging.logVerboseStart(options, targetPath.toString(), outputPaths.jsonPath.toString(),
                outputPaths.markdownPath == null ? null : outputPaths.markdownPath.toString(), logger);

        long subdirsStart = System.nanoTime();
        result.subdirectories = countImmediateSubdirectories(targetPath);
        result.timings.subdirsMs = elapsedMs(subdirsStart);

        Path existingOutputPath = findExistingOutputPath(outputPaths, options.overwrite);
        if (existingOutputPath != null) {
            result.skippedOutputPath = existingOutputPath;
            result.logs.addAll(logger.getLogs());
            return result;
        }

        logger.log("subdirectories=" + result.subdirectories);
        result.files = collectIndexFiles(targetPath, options, outputPaths, result.timings, logger);
        writeIndexOutputs(targetPath, result.files, options, outputPaths, result);
        result.timings.totalMs = elapsedMs(totalStart);
        Logging.logVerboseTimings(result, options, logger);
        result.logs.addAll(logger.getLogs());
        return result;
    }

    private OutputPaths getOutputPaths(Path targetPath, IndexgenOptions options) {
        Path jsonPath = targetPath.resolve(options.outputFileName).normalize();
        Path markdownPath = options.markdownOutput ? jsonPath.getParent().resolve(MARKDOWN_OUTPUT_FILE_NAME).normalize() : null;
        return new OutputPaths(jsonPath, markdownPath);
    }

    private boolean isGeneratedOutputPath(Path filePath, OutputPaths outputPaths) {
        Path resolvedFilePath = filePath.toAbsolutePath().normalize();
        if (resolvedFilePath.equals(outputPaths.jsonPath.toAbsolutePath().normalize())) {
            return true;
        }
        return outputPaths.markdownPath != null
                && resolvedFilePath.equals(outputPaths.markdownPath.toAbsolutePath().normalize());
    }

    private int countImmediateSubdirectories(Path targetPath) throws IOException {
        int count = 0;
        for (Path entry : listVisibleEntries(targetPath)) {
            if (Files.isDirectory(entry)) {
                count++;
            }
        }
        return count;
    }

    private Path findExistingOutputPath(OutputPaths outputPaths, boolean overwriteEnabled) {
        if (overwriteEnabled) {
            return null;
        }
        if (Files.isRegularFile(outputPaths.jsonPath)) {
            return outputPaths.jsonPath;
        }
        if (outputPaths.markdownPath != null && Files.isRegularFile(outputPaths.markdownPath)) {
            return outputPaths.markdownPath;
        }
        return null;
    }

    private IndexFile buildIndexFile(Path filePath, Path targetPath, String inputEncoding, List<String> jsonSummaryPaths,
            IndexgenTimings timings) throws IOException {
        long statStart = System.nanoTime();
        long size = Files.size(filePath);
        timings.statMs += elapsedMs(statStart);

        String ext = PathUtils.getFileExtension(filePath.toString());
        String summary = readSummary(filePath, ext, jsonSummaryPaths, inputEncoding, timings);

        IndexFile file = new IndexFile();
        file.name = PathUtils.getFileName(filePath.toString());
        file.path = PathUtils.toPosixPath(targetPath.relativize(filePath).toString());
        Path parent = filePath.getParent();
        file.dir = parent == null ? "" : PathUtils.toPosixPath(targetPath.relativize(parent).toString());
        file.ext = ext;
        file.size = size;
        file.summary = summary;
        return file;
    }

    private String readSummary(Path filePath, String ext, List<String> jsonSummaryPaths, String inputEncoding,
            IndexgenTimings timings) throws IOException {
        if ("md".equals(ext)) {
            return readMarkdownSummary(filePath, inputEncoding, timings);
        }
        if ("json".equals(ext) && jsonSummaryPaths != null && !jsonSummaryPaths.isEmpty()) {
            return readJsonSummary(filePath, inputEncoding, jsonSummaryPaths, timings);
        }
        return null;
    }

    private String readMarkdownSummary(Path filePath, String inputEncoding, IndexgenTimings timings) throws IOException {
        long readFileStart = System.nanoTime();
        String content = Encoding.readTextFile(filePath, inputEncoding);
        timings.readFileMs += elapsedMs(readFileStart);

        long summaryStart = System.nanoTime();
        String summary = Markdown.extractSummary(content);
        timings.summaryMs += elapsedMs(summaryStart);
        return summary;
    }

    private String readJsonSummary(Path filePath, String inputEncoding, List<String> jsonSummaryPaths, IndexgenTimings timings)
            throws IOException {
        long readFileStart = System.nanoTime();
        String content = Encoding.readTextFile(filePath, inputEncoding);
        timings.readFileMs += elapsedMs(readFileStart);

        long summaryStart = System.nanoTime();
        String summary = JsonSummary.extractJsonSummary(content, jsonSummaryPaths);
        timings.summaryMs += elapsedMs(summaryStart);
        return summary;
    }

    private List<Path> collectIndexableFilesWithSet(Path dirPath, boolean recursive, Set<String> includeExtensions,
            List<Path> entries) throws IOException {
        List<Path> files = new ArrayList<Path>();

        for (Path fullPath : entries) {
            if (Files.isDirectory(fullPath)) {
                if (recursive) {
                    files.addAll(collectIndexableFilesWithSet(fullPath, recursive, includeExtensions, listVisibleEntries(fullPath)));
                }
                continue;
            }
            if (!Files.isRegularFile(fullPath)) {
                continue;
            }

            String extension = PathUtils.getFileExtension(fullPath.toString());
            if (extension.length() > 0 && includeExtensions.contains(extension)) {
                files.add(fullPath);
            }
        }

        return files;
    }

    private List<IndexFile> collectIndexFiles(Path targetPath, IndexgenOptions options, OutputPaths outputPaths,
            IndexgenTimings timings, VerboseLogger logger) throws IOException {
        logger.log("scanning-dir=.");

        long collectStart = System.nanoTime();
        List<Path> indexableFiles = collectIndexableFiles(targetPath, options.recursive, options.includeExtensions);
        List<Path> filteredFiles = new ArrayList<Path>();
        for (Path filePath : indexableFiles) {
            if (!isGeneratedOutputPath(filePath, outputPaths)) {
                filteredFiles.add(filePath);
            }
        }
        timings.collectMs += elapsedMs(collectStart);

        List<IndexFile> files = new ArrayList<IndexFile>();
        for (Path filePath : filteredFiles) {
            IndexFile file = buildIndexFile(filePath, targetPath, options.inputEncoding, options.jsonSummaryPaths, timings);
            logger.log("found-file=" + file.path);
            files.add(file);
        }

        Collections.sort(files, new Comparator<IndexFile>() {
            @Override
            public int compare(IndexFile a, IndexFile b) {
                return compareJapanese(a.path, b.path);
            }
        });
        return files;
    }

    private void writeIndexOutputs(Path targetPath, List<IndexFile> files, IndexgenOptions options, OutputPaths outputPaths,
            IndexgenResult result) throws IOException {
        Files.createDirectories(outputPaths.jsonPath.getParent());

        long jsonStringifyStart = System.nanoTime();
        String jsonContent = buildIndexContent(options.title, targetPath, files, outputPaths.jsonPath,
                !Boolean.FALSE.equals(options.includeGeneratorMetadata));
        result.timings.jsonStringifyMs = elapsedMs(jsonStringifyStart);

        long jsonWriteStart = System.nanoTime();
        Encoding.writeTextFile(outputPaths.jsonPath, jsonContent, options.outputEncoding);
        result.timings.jsonWriteMs = elapsedMs(jsonWriteStart);
        result.generatedPaths.add(outputPaths.jsonPath);

        if (outputPaths.markdownPath == null) {
            return;
        }

        long markdownStart = System.nanoTime();
        Encoding.writeTextFile(outputPaths.markdownPath, Markdown.buildMarkdownIndexContent(files), options.outputEncoding);
        result.timings.markdownMs = elapsedMs(markdownStart);
        result.generatedPaths.add(outputPaths.markdownPath);
    }

    private List<Path> listVisibleEntries(Path dirPath) throws IOException {
        List<Path> entries = new ArrayList<Path>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath);
        try {
            for (Path entry : stream) {
                Path name = entry.getFileName();
                if (name == null || name.toString().startsWith(".")) {
                    continue;
                }
                entries.add(entry);
            }
        } finally {
            stream.close();
        }

        Collections.sort(entries, new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return compareJapanese(a.getFileName().toString(), b.getFileName().toString());
            }
        });
        return entries;
    }

    private static int compareJapanese(String a, String b) {
        synchronized (JAPANESE_COLLATOR) {
            int result = JAPANESE_COLLATOR.compare(a, b);
            return result != 0 ? result : a.compareTo(b);
        }
    }

    private String buildFilesJson(List<IndexFile> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (!files.isEmpty()) {
            builder.append('\n');
        }

        for (int i = 0; i < files.size(); i++) {
            IndexFile file = files.get(i);
            builder.append("    {\n");
            builder.append("      \"name\": ").append(quote(file.name)).append(",\n");
            builder.append("      \"path\": ").append(quote(file.path)).append(",\n");
            builder.append("      \"ext\": ").append(quote(file.ext)).append(",\n");
            builder.append("      \"dir\": ").append(quote(file.dir)).append(",\n");
            builder.append("      \"size\": ").append(file.size);
            if (file.summary != null) {
                builder.append(",\n");
                builder.append("      \"summary\": ").append(quote(file.summary)).append('\n');
            } else {
                builder.append('\n');
            }
            builder.append("    }");
            if (i + 1 < files.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }

        builder.append("  ]");
        return builder.toString();
    }

    private String quote(String value) {
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

    private double elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }
}
