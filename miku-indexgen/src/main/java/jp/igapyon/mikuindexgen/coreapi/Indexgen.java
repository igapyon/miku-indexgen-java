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
    private static final String JSON_OUTPUT_FILE_NAME = "index.json";
    private static final String MARKDOWN_OUTPUT_FILE_NAME = "index.md";
    private static final Collator JAPANESE_COLLATOR = Collator.getInstance(Locale.JAPANESE);

    public List<Path> collectIndexableFiles(Path dirPath, boolean recursive, List<String> includeExtensions) throws IOException {
        Set<String> allowedExtensions = new LinkedHashSet<String>(includeExtensions);
        return collectIndexableFilesWithSet(dirPath, recursive, allowedExtensions, listVisibleEntries(dirPath));
    }

    public String buildIndexContent(String title, Path targetPath, List<IndexFile> files, Path outputPath,
            boolean includeGeneratorMetadata) {
        String basePath = PathUtils.toPosixPath(outputPath.getParent().relativize(targetPath).toString());
        if (basePath.length() == 0) {
            basePath = ".";
        }
        return formatIndexJson(title, includeGeneratorMetadata ? GENERATOR_NAME : null, basePath, files);
    }

    public String formatIndexJson(String title, String generator, String basePath, List<IndexFile> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        if (title != null) {
            builder.append(" \"title\": ").append(quote(title)).append(",\n");
        }
        if (generator != null) {
            builder.append(" \"generator\": ").append(quote(generator)).append(",\n");
        }
        builder.append(" \"basePath\": ").append(quote(basePath)).append(",\n");
        builder.append(" \"files\": ").append(buildFilesJson(files)).append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    public IndexgenResult createIndexes(IndexgenOptions options) throws IOException {
        validateInputMode(options);
        if (hasValue(options.inputParentDirectory)) {
            return createIndexesForChildDirectories(options);
        }
        return createIndexesForSingleInputDirectory(options);
    }

    private IndexgenResult createIndexesForSingleInputDirectory(IndexgenOptions options) throws IOException {
        long totalStart = System.nanoTime();
        Path inputDirectoryPath = Paths.get(options.inputDirectory).toAbsolutePath().normalize();

        if (!Files.isDirectory(inputDirectoryPath)) {
            throw new IllegalArgumentException("Input directory does not exist: " + inputDirectoryPath);
        }

        IndexgenResult result = new IndexgenResult();
        OutputPaths outputPaths = getOutputPaths(inputDirectoryPath, options);
        result.jsonPath = outputPaths.jsonPath;
        result.markdownPath = outputPaths.markdownPath;

        VerboseLogger logger = new VerboseLogger(options.verbose, options.verboseStream);
        Logging.logVerboseStart(options, inputDirectoryPath.toString(), outputPaths.jsonPath.toString(),
                outputPaths.markdownPath == null ? null : outputPaths.markdownPath.toString(), logger);

        long subdirsStart = System.nanoTime();
        result.subdirectories = countImmediateSubdirectories(inputDirectoryPath);
        result.timings.subdirsMs = elapsedMs(subdirsStart);

        Path existingOutputPath = findExistingOutputPath(outputPaths, options.overwrite);
        if (existingOutputPath != null) {
            result.skippedOutputPath = existingOutputPath;
            result.logs.addAll(logger.getLogs());
            return result;
        }

        logger.log("subdirectories=" + result.subdirectories);
        result.files = collectIndexFiles(inputDirectoryPath, options, outputPaths, result.timings, logger);
        writeIndexOutputs(inputDirectoryPath, result.files, options, outputPaths, result);
        result.timings.totalMs = elapsedMs(totalStart);
        Logging.logVerboseTimings(result, options, logger);
        result.logs.addAll(logger.getLogs());
        return result;
    }

    private IndexgenResult createIndexesForChildDirectories(IndexgenOptions options) throws IOException {
        Path inputParentDirectoryPath = Paths.get(options.inputParentDirectory).toAbsolutePath().normalize();
        if (!Files.isDirectory(inputParentDirectoryPath)) {
            throw new IllegalArgumentException("Input parent directory does not exist: " + inputParentDirectoryPath);
        }

        Path sharedOutputDirectory = resolveBatchSharedOutputDirectory(options.outputDirectory);
        List<Path> childDirectories = collectChildBaseDirectories(inputParentDirectoryPath, sharedOutputDirectory);

        IndexgenResult result = new IndexgenResult();
        result.childDirectoriesProcessed = childDirectories.size();
        result.subdirectories = childDirectories.size();

        for (Path childDirectory : childDirectories) {
            IndexgenOptions childOptions = copyOptionsForChildDirectory(options, childDirectory,
                    resolveChildOutputDirectory(sharedOutputDirectory, childDirectory));
            IndexgenResult childResult = createIndexesForSingleInputDirectory(childOptions);
            result.files.addAll(childResult.files);
            result.generatedPaths.addAll(childResult.generatedPaths);
            result.logs.addAll(childResult.logs);
            if (childResult.skipped()) {
                result.logs.add("skip: " + childResult.skippedOutputPath);
            }
        }

        return result;
    }

    private void validateInputMode(IndexgenOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Options are required.");
        }
        boolean hasInputDirectory = hasValue(options.inputDirectory);
        boolean hasInputParentDirectory = hasValue(options.inputParentDirectory);
        if (hasInputDirectory == hasInputParentDirectory) {
            throw new IllegalArgumentException("Specify either inputDirectory or inputParentDirectory.");
        }
    }

    private boolean hasValue(String value) {
        return value != null && value.length() > 0;
    }

    private OutputPaths getOutputPaths(Path inputDirectoryPath, IndexgenOptions options) throws IOException {
        Path outputDirectoryPath = resolveOutputDirectory(inputDirectoryPath, options.outputDirectory);
        Path jsonPath = outputDirectoryPath.resolve(JSON_OUTPUT_FILE_NAME).normalize();
        Path markdownPath = options.markdownOutput ? outputDirectoryPath.resolve(MARKDOWN_OUTPUT_FILE_NAME).normalize() : null;
        return new OutputPaths(jsonPath, markdownPath);
    }

    private Path resolveOutputDirectory(Path inputDirectoryPath, String outputDirectory) throws IOException {
        if (outputDirectory == null || outputDirectory.length() == 0) {
            return inputDirectoryPath;
        }
        Path outputDirectoryPath = Paths.get(outputDirectory).toAbsolutePath().normalize();
        if (Files.exists(outputDirectoryPath) && !Files.isDirectory(outputDirectoryPath)) {
            throw new IllegalArgumentException("Output directory must be a directory: " + outputDirectoryPath);
        }
        return outputDirectoryPath;
    }

    private Path resolveBatchSharedOutputDirectory(String outputDirectory) throws IOException {
        if (outputDirectory == null || outputDirectory.length() == 0) {
            return null;
        }
        Path outputDirectoryPath = Paths.get(outputDirectory).toAbsolutePath().normalize();
        if (Files.exists(outputDirectoryPath) && !Files.isDirectory(outputDirectoryPath)) {
            throw new IllegalArgumentException("Output directory must be a directory: " + outputDirectoryPath);
        }
        return outputDirectoryPath;
    }

    private List<Path> collectChildBaseDirectories(Path inputParentDirectoryPath, Path sharedOutputDirectory) throws IOException {
        List<Path> childDirectories = new ArrayList<Path>();
        for (Path entry : listVisibleEntries(inputParentDirectoryPath)) {
            if (!Files.isDirectory(entry)) {
                continue;
            }
            if (isSharedOutputChildDirectory(entry, inputParentDirectoryPath, sharedOutputDirectory)) {
                continue;
            }
            childDirectories.add(entry);
        }
        return childDirectories;
    }

    private boolean isSharedOutputChildDirectory(Path childDirectory, Path inputParentDirectoryPath, Path sharedOutputDirectory) {
        if (sharedOutputDirectory == null) {
            return false;
        }
        if (!sharedOutputDirectory.startsWith(inputParentDirectoryPath)) {
            return false;
        }
        Path relativeOutputPath = inputParentDirectoryPath.relativize(sharedOutputDirectory);
        if (relativeOutputPath.getNameCount() != 1) {
            return false;
        }
        return childDirectory.toAbsolutePath().normalize().equals(sharedOutputDirectory);
    }

    private Path resolveChildOutputDirectory(Path sharedOutputDirectory, Path childDirectory) {
        if (sharedOutputDirectory == null) {
            return null;
        }
        Path childName = childDirectory.getFileName();
        return childName == null ? sharedOutputDirectory : sharedOutputDirectory.resolve(childName.toString());
    }

    private IndexgenOptions copyOptionsForChildDirectory(IndexgenOptions options, Path childDirectory, Path childOutputDirectory) {
        IndexgenOptions childOptions = new IndexgenOptions();
        childOptions.inputDirectory = childDirectory.toString();
        childOptions.outputDirectory = childOutputDirectory == null ? null : childOutputDirectory.toString();
        childOptions.title = options.title;
        childOptions.markdownOutput = options.markdownOutput;
        childOptions.includeGeneratorMetadata = options.includeGeneratorMetadata;
        childOptions.jsonSummaryPaths = copyList(options.jsonSummaryPaths);
        childOptions.recursive = options.recursive;
        childOptions.overwrite = options.overwrite;
        childOptions.verbose = options.verbose;
        childOptions.verboseStream = options.verboseStream;
        childOptions.includeExtensions = copyList(options.includeExtensions);
        childOptions.inputEncoding = options.inputEncoding;
        childOptions.outputEncoding = options.outputEncoding;
        return childOptions;
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
            logger.log("reading-file=" + PathUtils.toPosixPath(targetPath.relativize(filePath).toString()));
            IndexFile file = buildIndexFile(filePath, targetPath, options.inputEncoding, options.jsonSummaryPaths, timings);
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
        builder.append("[\n");

        for (int i = 0; i < files.size(); i++) {
            IndexFile file = files.get(i);
            builder.append("  {");
            builder.append(quote("name")).append(":").append(quote(file.name)).append(",");
            builder.append(quote("path")).append(":").append(quote(file.path)).append(",");
            builder.append(quote("ext")).append(":").append(quote(file.ext)).append(",");
            builder.append(quote("dir")).append(":").append(quote(file.dir)).append(",");
            builder.append(quote("size")).append(":").append(file.size);
            if (file.summary != null) {
                builder.append(",").append(quote("summary")).append(":").append(quote(file.summary));
            }
            builder.append("}");
            if (i + 1 < files.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }

        builder.append(" ]");
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

    private List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }
}
