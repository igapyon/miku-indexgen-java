package jp.igapyon.mikuindexgen.coreapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuindexgen.encoding.Encoding;
import jp.igapyon.mikuindexgen.model.IndexFile;

class IndexgenTest {
    @TempDir
    Path tempDir;

    @Test
    void createIndexesCreatesOneRootIndexFileIncludingFilesInTheTargetDirectory() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path chapter1 = docsDir.resolve("chapter1");
        Path chapter2 = docsDir.resolve("chapter2");

        Files.createDirectories(chapter1.resolve("nested"));
        Files.createDirectories(chapter2);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(chapter1.resolve("a.md"), "# A\n".getBytes("UTF-8"));
        Files.write(chapter1.resolve("nested").resolve("b.md"), "# B\n".getBytes("UTF-8"));
        Files.write(chapter2.resolve("c.md"), "Workbook: sample.xlsx\nSecond line\n# C\n".getBytes("UTF-8"));
        Files.write(chapter2.resolve("data.json"), "{\n  \"title\": \"Data\"\n}\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        IndexgenResult result = new Indexgen().createIndexes(options);

        String index = new String(Files.readAllBytes(docsDir.resolve("index.json")), "UTF-8");
        assertEquals(2, result.subdirectories);
        assertTrue(index.contains("\"generator\": \"miku-indexgen\""));
        assertTrue(index.contains("\"generation\": {\"schemaVersion\":1,\"inputPath\":\".\",\"markdownOutput\":false,\"recursive\":true,\"includeExtensions\":[\"md\",\"json\"],\"inputEncoding\":\"utf8\",\"outputEncoding\":\"utf8\",\"includeGeneratorMetadata\":true}"));
        assertTrue(index.contains("\"basePath\": \".\""));
        assertTrue(index.contains("\"path\":\"chapter1/a.md\""));
        assertTrue(index.contains("\"summary\":\"Workbook: sample.xlsx Second line\""));
        assertTrue(index.contains("\"path\":\"root.md\""));
        assertEquals(Arrays.asList("chapter1/a.md", "chapter1/nested/b.md", "chapter2/c.md", "chapter2/data.json", "root.md"),
                paths(result.files));
    }

    @Test
    void createIndexesWritesMarkdownOutputWhenEnabled() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path chapter1 = docsDir.resolve("chapter1");

        Files.createDirectories(chapter1);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(chapter1.resolve("a.md"), "# A\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.markdownOutput = true;
        new Indexgen().createIndexes(options);

        String markdown = new String(Files.readAllBytes(docsDir.resolve("index.md")), "UTF-8");
        assertTrue(markdown.contains("| [root.md](root.md) | md |  | 7 | Root |"));
        assertTrue(markdown.contains("| [chapter1/a.md](chapter1/a.md) | md | chapter1 | 4 | A |"));
    }

    @Test
    void createIndexesReportsAddNoneAndUpdateStatusesWhenOutputsAreRewritten() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path chapter1 = docsDir.resolve("chapter1");

        Files.createDirectories(chapter1);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(chapter1.resolve("a.md"), "# A\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.markdownOutput = true;

        Indexgen indexgen = new Indexgen();
        IndexgenResult firstResult = indexgen.createIndexes(options);
        assertEquals(Arrays.asList(
                "add   : " + docsDir.resolve("index.json").toAbsolutePath().normalize(),
                "add   : " + docsDir.resolve("index.md").toAbsolutePath().normalize()),
                firstResult.outputMessages);

        IndexgenResult secondResult = indexgen.createIndexes(options);
        assertEquals(Arrays.asList(
                "none  : " + docsDir.resolve("index.json").toAbsolutePath().normalize(),
                "none  : " + docsDir.resolve("index.md").toAbsolutePath().normalize()),
                secondResult.outputMessages);

        Files.write(chapter1.resolve("a.md"), "# A updated\n".getBytes("UTF-8"));
        IndexgenResult thirdResult = indexgen.createIndexes(options);
        assertEquals(Arrays.asList(
                "update: " + docsDir.resolve("index.json").toAbsolutePath().normalize(),
                "update: " + docsDir.resolve("index.md").toAbsolutePath().normalize()),
                thirdResult.outputMessages);
    }

    @Test
    void createIndexesWritesOutputsUnderOutputDirectoryWhenSpecified() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path outDir = tempDir.resolve("out");
        Path chapter1 = docsDir.resolve("chapter1");

        Files.createDirectories(chapter1);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(chapter1.resolve("a.md"), "# A\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.outputDirectory = outDir.toString();
        options.markdownOutput = true;
        new Indexgen().createIndexes(options);

        assertTrue(Files.isRegularFile(outDir.resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("index.md")));
        assertFalse(Files.exists(docsDir.resolve("index.json")));
        String index = new String(Files.readAllBytes(outDir.resolve("index.json")), "UTF-8");
        assertTrue(index.contains("\"basePath\": \"../docs\""));
    }

    @Test
    void buildIndexContentFormatsEachFileEntryOnOneLineForSearchFriendlyJson() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path outDir = tempDir.resolve("out");
        Files.createDirectories(outDir);

        IndexFile first = new IndexFile();
        first.name = "a.md";
        first.path = "chapter1/a.md";
        first.ext = "md";
        first.dir = "chapter1";
        first.size = 4;
        first.summary = "A";

        IndexFile second = new IndexFile();
        second.name = "data.json";
        second.path = "chapter2/data.json";
        second.ext = "json";
        second.dir = "chapter2";
        second.size = 22;

        String content = new Indexgen().buildIndexContent("Docs Index", docsDir,
                Arrays.asList(first, second), outDir.resolve("index.json"), true);

        assertEquals(String.join("\n", Arrays.asList(
                "{",
                " \"title\": \"Docs Index\",",
                " \"generator\": \"miku-indexgen\",",
                " \"basePath\": \"../docs\",",
                " \"files\": [",
                "  {\"name\":\"a.md\",\"path\":\"chapter1/a.md\",\"ext\":\"md\",\"dir\":\"chapter1\",\"size\":4,\"summary\":\"A\"},",
                "  {\"name\":\"data.json\",\"path\":\"chapter2/data.json\",\"ext\":\"json\",\"dir\":\"chapter2\",\"size\":22}",
                " ]",
                "}",
                "")), content);
    }

    @Test
    void createIndexesIncludesDocumentedMarkdownFrontMatterMetadataInFileEntries() throws Exception {
        Path docsDir = tempDir.resolve("docs");

        Files.createDirectories(docsDir);
        Files.write(docsDir.resolve("writing-guide.md"), String.join("\n",
                "---",
                "title: Writing Guide",
                "description: >",
                "  Practical writing conventions for indexed Markdown files.",
                "topics:",
                "  - writing",
                "  - article",
                "  - tone",
                "category: guide",
                "status: stable",
                "audience: [agent, maintainer]",
                "created: 2026-05-22",
                "updated: 2026-05-23",
                "sources:",
                "  - type: human-input",
                "    label: user-provided requirements",
                "    role: primary",
                "    checked: 2026-05-22",
                "---",
                "",
                "# Body Title",
                "").getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        IndexgenResult result = new Indexgen().createIndexes(options);

        String index = new String(Files.readAllBytes(docsDir.resolve("index.json")), "UTF-8");
        assertEquals("Writing Guide", result.files.get(0).title);
        assertEquals("Practical writing conventions for indexed Markdown files.", result.files.get(0).description);
        assertEquals(Arrays.asList("writing", "article", "tone"), result.files.get(0).topics);
        assertEquals("guide", result.files.get(0).category);
        assertEquals("stable", result.files.get(0).status);
        assertEquals(Arrays.asList("agent", "maintainer"), result.files.get(0).audience);
        assertEquals("2026-05-22", result.files.get(0).created);
        assertEquals("2026-05-23", result.files.get(0).updated);
        assertEquals("human-input", result.files.get(0).sources.get(0).type);
        assertEquals("Body Title", result.files.get(0).summary);
        assertTrue(index.contains("\"title\":\"Writing Guide\""));
        assertTrue(index.contains("\"description\":\"Practical writing conventions for indexed Markdown files.\""));
        assertTrue(index.contains("\"topics\":[\"writing\",\"article\",\"tone\"]"));
        assertTrue(index.contains("\"category\":\"guide\""));
        assertTrue(index.contains("\"audience\":[\"agent\",\"maintainer\"]"));
        assertTrue(index.contains("\"sources\":[{\"type\":\"human-input\",\"role\":\"primary\",\"label\":\"user-provided requirements\",\"checked\":\"2026-05-22\"}]"));
        assertTrue(index.contains("\"summary\":\"Body Title\""));
    }

    @Test
    void refreshIndexRegeneratesAnExistingIndexFromGenerationMetadata() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path outDir = tempDir.resolve("out");

        Files.createDirectories(docsDir);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.outputDirectory = outDir.toString();
        options.title = "Docs Index";
        options.markdownOutput = true;
        options.includeExtensions = Arrays.asList("md");
        new Indexgen().createIndexes(options);

        Files.write(docsDir.resolve("second.md"), "# Second\n".getBytes("UTF-8"));

        IndexgenOptions refreshOptions = new IndexgenOptions();
        refreshOptions.refreshIndex = outDir.resolve("index.json").toString();
        refreshOptions.overwrite = true;
        refreshOptions.verbose = false;
        IndexgenResult result = new Indexgen().createIndexes(refreshOptions);

        String index = new String(Files.readAllBytes(outDir.resolve("index.json")), "UTF-8");
        String markdown = new String(Files.readAllBytes(outDir.resolve("index.md")), "UTF-8");
        assertEquals(Arrays.asList("root.md", "second.md"), paths(result.files));
        assertTrue(index.contains("\"title\": \"Docs Index\""));
        assertTrue(index.contains("\"inputPath\":\"../docs\""));
        assertTrue(index.contains("\"markdownOutput\":true"));
        assertTrue(index.contains("\"includeExtensions\":[\"md\"]"));
        assertTrue(markdown.contains("| [second.md](second.md) | md |  | 9 | Second |"));
    }

    @Test
    void createIndexesExcludesFilesByRecursiveInputRelativeGlobAfterExtensionFiltering() throws Exception {
        Path docsDir = tempDir.resolve("docs");

        Files.createDirectories(docsDir.resolve("2026").resolve("05").resolve("images-ai-native").resolve("src")
                .resolve("sections").resolve("001"));
        Files.createDirectories(docsDir.resolve("2026").resolve("05").resolve("article"));
        Files.write(docsDir.resolve("2026").resolve("05").resolve("article").resolve("main.md"),
                "# Main\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("2026").resolve("05").resolve("article").resolve("note-image-recovery.md"),
                "# Recovery\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("2026").resolve("05").resolve("images-ai-native").resolve("src")
                .resolve("sections").resolve("001").resolve("image-prompt.md"), "# Prompt\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("2026").resolve("05").resolve("images-ai-native").resolve("src")
                .resolve("sections").resolve("001").resolve("section-text.md"), "# Section\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("2026").resolve("05").resolve("article").resolve("data.json"),
                "{\"title\":\"Data\"}\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.includeExtensions = Arrays.asList("md");
        options.excludeGlobs = Arrays.asList(
                "**/images-*/**",
                "**/note-image-recovery.md",
                "**/image-prompt.md",
                "**/section-text.md");

        IndexgenResult result = new Indexgen().createIndexes(options);
        String index = new String(Files.readAllBytes(docsDir.resolve("index.json")), "UTF-8");

        assertEquals(Arrays.asList("2026/05/article/main.md"), paths(result.files));
        assertTrue(index.contains("\"includeExtensions\":[\"md\"]"));
        assertTrue(index.contains("\"excludeGlobs\":[\"**/images-*/**\",\"**/note-image-recovery.md\",\"**/image-prompt.md\",\"**/section-text.md\"]"));
        assertFalse(index.contains("data.json"));
        assertFalse(index.contains("\"path\":\"2026/05/article/note-image-recovery.md\""));
        assertFalse(index.contains("\"path\":\"2026/05/images-ai-native/src/sections/001/image-prompt.md\""));
        assertFalse(index.contains("\"path\":\"2026/05/images-ai-native/src/sections/001/section-text.md\""));
    }

    @Test
    void buildIndexContentEscapesLineBreaksInsideFileEntriesWithoutSplittingTheRecordLine() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Files.createDirectories(docsDir);

        IndexFile file = new IndexFile();
        file.name = "a.md";
        file.path = "a.md";
        file.ext = "md";
        file.dir = "";
        file.size = 12;
        file.summary = "First line\nSecond line";

        String content = new Indexgen().buildIndexContent(null, docsDir,
                Arrays.asList(file), docsDir.resolve("index.json"), true);

        assertTrue(content
                .contains("  {\"name\":\"a.md\",\"path\":\"a.md\",\"ext\":\"md\",\"dir\":\"\",\"size\":12,\"summary\":\"First line\\nSecond line\"}"));
        assertEquals(8, content.split("\n", -1).length);
    }

    @Test
    void createIndexesProcessesEachDirectChildDirectoryInChildDirectoryBatchMode() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Files.createDirectories(child1.resolve("nested"));
        Files.createDirectories(child2);
        Files.createDirectories(parentDir.resolve(".hidden-child"));
        Files.write(parentDir.resolve("note.md"), "# Parent\n".getBytes("UTF-8"));
        Files.write(child1.resolve("a.md"), "# A\n".getBytes("UTF-8"));
        Files.write(child1.resolve("nested").resolve("deep.md"), "# Deep\n".getBytes("UTF-8"));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes("UTF-8"));

        IndexgenOptions options = new IndexgenOptions();
        options.inputParentDirectory = parentDir.toString();
        options.markdownOutput = true;
        options.recursive = true;
        options.overwrite = true;
        options.verbose = false;
        options.includeExtensions = Arrays.asList("md", "json");
        options.inputEncoding = "utf8";
        options.outputEncoding = "utf8";

        IndexgenResult result = new Indexgen().createIndexes(options);

        assertEquals(2, result.childDirectoriesProcessed);
        assertTrue(Files.isRegularFile(child1.resolve("index.json")));
        assertTrue(Files.isRegularFile(child2.resolve("index.json")));
        assertFalse(Files.exists(parentDir.resolve("index.json")));
        assertFalse(Files.exists(parentDir.resolve(".hidden-child").resolve("index.json")));
        String child1Index = new String(Files.readAllBytes(child1.resolve("index.json")), "UTF-8");
        assertTrue(child1Index.contains("\"path\":\"nested/deep.md\""));
    }

    @Test
    void createIndexesWritesChildDirectoryBatchOutputsUnderSharedOutputDirectoryWhenSpecified() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path outDir = tempDir.resolve("out");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Files.createDirectories(child1);
        Files.createDirectories(child2);
        Files.write(child1.resolve("a.md"), "# A\n".getBytes("UTF-8"));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes("UTF-8"));

        IndexgenOptions options = new IndexgenOptions();
        options.inputParentDirectory = parentDir.toString();
        options.outputDirectory = outDir.toString();
        options.markdownOutput = true;
        options.recursive = false;
        options.overwrite = true;
        options.includeExtensions = Arrays.asList("md", "json");
        options.inputEncoding = "utf8";
        options.outputEncoding = "utf8";

        new Indexgen().createIndexes(options);

        assertTrue(Files.isRegularFile(outDir.resolve("b1").resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("b2").resolve("index.json")));
        assertFalse(Files.exists(child1.resolve("index.json")));
        String index = new String(Files.readAllBytes(outDir.resolve("b1").resolve("index.json")), "UTF-8");
        assertTrue(index.contains("\"basePath\": \"../../parent/b1\""));
    }

    @Test
    void createIndexesAggregatesChildDirectoryBatchFailuresAndContinues() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path outDir = tempDir.resolve("out");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Path child3 = parentDir.resolve("b3");
        Files.createDirectories(child1);
        Files.createDirectories(child2);
        Files.createDirectories(child3);
        Files.createDirectories(outDir);
        Files.write(child1.resolve("a.md"), "# A\n".getBytes("UTF-8"));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes("UTF-8"));
        Files.write(child3.resolve("c.md"), "# C\n".getBytes("UTF-8"));
        Files.write(outDir.resolve("b2"), "not a directory\n".getBytes("UTF-8"));

        IndexgenOptions options = new IndexgenOptions();
        options.inputParentDirectory = parentDir.toString();
        options.outputDirectory = outDir.toString();
        options.markdownOutput = true;
        options.recursive = false;
        options.overwrite = true;
        options.includeExtensions = Arrays.asList("md", "json");
        options.inputEncoding = "utf8";
        options.outputEncoding = "utf8";

        IndexgenBatchException ex = assertThrows(IndexgenBatchException.class,
                () -> new Indexgen().createIndexes(options));
        IndexgenResult result = ex.getResult();

        assertEquals(3, result.childDirectoriesProcessed);
        assertEquals(1, result.childDirectoriesFailed);
        assertTrue(result.failed());
        assertTrue(Files.isRegularFile(outDir.resolve("b1").resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("b3").resolve("index.json")));
        assertFalse(Files.isDirectory(outDir.resolve("b2")));
        assertTrue(result.childFailureMessages.get(0).contains("Output directory must be a directory"));
    }

    @Test
    void createIndexesDoesNotOverwriteExistingOutputWhenDisabled() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Files.createDirectories(docsDir);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("index.json"), "keep me\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.overwrite = false;
        IndexgenResult result = new Indexgen().createIndexes(options);

        assertTrue(result.skipped());
        assertEquals("keep me\n", new String(Files.readAllBytes(docsDir.resolve("index.json")), "UTF-8"));
    }

    @Test
    void createIndexesExcludesGeneratedJsonAndMarkdownOutputsFromTheGeneratedFileList() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Files.createDirectories(docsDir);
        Files.write(docsDir.resolve("root.md"), "# Root\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("index.json"), "{}\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("index.md"), "# Previous Index\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.markdownOutput = true;
        IndexgenResult result = new Indexgen().createIndexes(options);

        assertEquals(Arrays.asList("root.md"), paths(result.files));
    }

    @Test
    void createIndexesReadsAndWritesShiftJisWhenRequested() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path chapter1 = docsDir.resolve("chapter1");

        Files.createDirectories(chapter1);
        Files.write(chapter1.resolve("a.md"), "# 日本語\n本文\n".getBytes(Charset.forName("Windows-31J")));

        IndexgenOptions options = defaultOptions(docsDir);
        options.title = "資料一覧";
        options.markdownOutput = true;
        options.inputEncoding = "shift_jis";
        options.outputEncoding = "shift_jis";
        new Indexgen().createIndexes(options);

        assertTrue(Encoding.readTextFile(docsDir.resolve("index.json"), "shift_jis").contains("\"title\": \"資料一覧\""));
        assertTrue(Encoding.readTextFile(docsDir.resolve("index.md"), "shift_jis")
                .contains("| [chapter1/a.md](chapter1/a.md) | md | chapter1 |"));
    }

    @Test
    void createIndexesReturnsVerboseLogsWithoutWritingDirectlyToStdout() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path chapter1 = docsDir.resolve("chapter1");

        Files.createDirectories(chapter1);
        Files.write(chapter1.resolve("a.md"), "# A\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        options.title = "Verbose Docs";
        options.verbose = true;
        options.jsonSummaryPaths = Arrays.asList("/title", "/name");
        IndexgenResult result = new Indexgen().createIndexes(options);

        assertTrue(result.logs.contains("verbose: title=Verbose Docs"));
        assertTrue(result.logs.contains("verbose: include-ext=md,json"));
        assertTrue(result.logs.contains("verbose: json-summary-path=/title,/name"));
        assertTrue(result.logs.contains("verbose: subdirectories=1"));
        assertTrue(result.logs.contains("verbose: scanning-dir=."));
        assertTrue(result.logs.contains("verbose: reading-file=chapter1/a.md"));
        assertTrue(startsWith(result.logs, "verbose: timing.json.write="));
        assertFalse(result.generatedPaths.isEmpty());
    }

    @Test
    void createIndexesSortsFilePathsByPosixRelativePathUsingUtf16CodeUnitOrder() throws Exception {
        Path docsDir = tempDir.resolve("docs");

        Files.createDirectories(docsDir.resolve("B"));
        Files.createDirectories(docsDir.resolve("a"));
        Files.createDirectories(docsDir.resolve("あ"));
        Files.write(docsDir.resolve("B").resolve("file.md"), "# B\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("a").resolve("file.md"), "# A\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("あ").resolve("file.md"), "# Japanese\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("Z.md"), "# Upper\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("a.md"), "# Lower\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        IndexgenResult result = new Indexgen().createIndexes(options);

        assertEquals(Arrays.asList("B/file.md", "Z.md", "a.md", "a/file.md", "あ/file.md"), paths(result.files));
    }

    private IndexgenOptions defaultOptions(Path docsDir) {
        IndexgenOptions options = new IndexgenOptions();
        options.inputDirectory = docsDir.toString();
        options.markdownOutput = false;
        options.recursive = true;
        options.overwrite = true;
        options.verbose = false;
        options.includeExtensions = Arrays.asList("md", "json");
        options.inputEncoding = "utf8";
        options.outputEncoding = "utf8";
        return options;
    }

    private List<String> paths(List<IndexFile> files) {
        java.util.ArrayList<String> paths = new java.util.ArrayList<String>();
        for (IndexFile file : files) {
            paths.add(file.path);
        }
        return paths;
    }

    private boolean startsWith(List<String> values, String prefix) {
        for (String value : values) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
