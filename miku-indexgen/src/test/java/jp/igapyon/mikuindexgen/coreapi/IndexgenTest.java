package jp.igapyon.mikuindexgen.coreapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(index.contains("\"basePath\": \".\""));
        assertTrue(index.contains("\"path\": \"chapter1/a.md\""));
        assertTrue(index.contains("\"summary\": \"Workbook: sample.xlsx Second line\""));
        assertTrue(index.contains("\"path\": \"root.md\""));
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
        assertTrue(child1Index.contains("\"path\": \"nested/deep.md\""));
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
    void createIndexesSortsFilePathsUsingJapaneseLocaleOrder() throws Exception {
        Path docsDir = tempDir.resolve("docs");

        Files.createDirectories(docsDir);
        Files.write(docsDir.resolve("b.md"), "# B\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("a.md"), "# A\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("い.md"), "# Hiragana I\n".getBytes("UTF-8"));
        Files.write(docsDir.resolve("あ.md"), "# Hiragana\n".getBytes("UTF-8"));

        IndexgenOptions options = defaultOptions(docsDir);
        IndexgenResult result = new Indexgen().createIndexes(options);

        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.JAPANESE);
        java.util.ArrayList<String> expected = new java.util.ArrayList<String>(
                Arrays.asList("b.md", "a.md", "い.md", "あ.md"));
        java.util.Collections.sort(expected, new java.util.Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                int result = collator.compare(left, right);
                return result != 0 ? result : left.compareTo(right);
            }
        });

        assertEquals(expected, paths(result.files));
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
