package jp.igapyon.mikuindexgen.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuindexgen.model.CliOptions;

class MikuIndexgenCliTest {
    @TempDir
    Path tempDir;

    @Test
    void parseArgsParsesTheInputDirectoryAndOptions() {
        CliOptions options = MikuIndexgenCli.parseArgs(new String[] {
                "--input-directory", "./docs",
                "--output-directory", "./out",
                "--title", "Docs Index",
                "--markdown",
                "--no-generator",
                "--json-summary-path", "/title,/metadata/name",
                "--no-recursive",
                "--no-overwrite",
                "--include-ext", "md,json",
                "--exclude-glob", " **\\images\\* ",
                "--exclude-glob", "**/images/*",
                "--input-encoding", "ShiftJIS",
                "--output-encoding", "shift-jis",
                "--verbose"
        });

        assertEquals("./docs", options.inputDirectory);
        assertEquals("./out", options.outputDirectory);
        assertEquals("Docs Index", options.title);
        assertTrue(options.markdownOutput);
        assertFalse(options.includeGeneratorMetadata.booleanValue());
        assertEquals(Arrays.asList("/title", "/metadata/name"), options.jsonSummaryPaths);
        assertFalse(options.recursive);
        assertFalse(options.overwrite);
        assertTrue(options.verbose);
        assertEquals(Arrays.asList("md", "json"), options.includeExtensions);
        assertEquals(Arrays.asList("**/images/*"), options.excludeGlobs);
        assertEquals("shift_jis", options.inputEncoding);
        assertEquals("shift_jis", options.outputEncoding);
    }

    @Test
    void parseArgsParsesRefreshIndexWithoutRequiringAnInputDirectory() {
        CliOptions options = MikuIndexgenCli.parseArgs(new String[] {
                "--refresh-index", "workplace/index.json",
                "--verbose"
        });

        assertEquals("workplace/index.json", options.refreshIndex);
        assertTrue(options.verbose);
    }

    @Test
    void parseArgsEnablesGeneratorMetadataByDefault() {
        assertTrue(MikuIndexgenCli.parseArgs(new String[] { "--input-directory", "./docs" }).includeGeneratorMetadata.booleanValue());
    }

    @Test
    void parseIncludeExtensionsNormalizesACommaSeparatedExtensionList() {
        assertEquals(Arrays.asList("md", "json"), MikuIndexgenCli.parseIncludeExtensions(".MD, json ,md"));
    }

    @Test
    void parseArgsSignalsHelpRequestsWithoutExitingFromTheParser() {
        assertThrows(HelpRequestedException.class, () -> MikuIndexgenCli.parseArgs(new String[] { "--help" }));
    }

    @Test
    void parseArgsSignalsVersionRequestsWithoutRequiringAnInputDirectory() {
        assertThrows(VersionRequestedException.class, () -> MikuIndexgenCli.parseArgs(new String[] { "--version" }));
    }

    @Test
    void runPrintsVersion() throws Exception {
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--version" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        assertEquals(0, exitCode);
        assertEquals("miku-indexgen 1.6.2\n", stdoutBuffer.toString("UTF-8"));
        assertEquals("", stderrBuffer.toString("UTF-8"));
    }

    @Test
    void runPrintsContractFocusedHelp() throws Exception {
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--help" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        String stdout = stdoutBuffer.toString("UTF-8");
        assertEquals(0, exitCode);
        assertEquals("", stderrBuffer.toString("UTF-8"));
        assertEquals(expectedHelpText(), stdout);
    }

    @Test
    void parseArgsParsesTheInputParentDirectory() {
        CliOptions options = MikuIndexgenCli.parseArgs(new String[] {
                "--input-parent-directory", "./parent",
                "--output-directory", "./out",
                "--no-recursive",
                "--verbose"
        });

        assertEquals("./parent", options.inputParentDirectory);
        assertEquals("./out", options.outputDirectory);
        assertFalse(options.recursive);
        assertTrue(options.verbose);
    }

    @Test
    void parseArgsRejectsUsingMultipleInputModesTogether() {
        assertThrows(IllegalArgumentException.class, () -> MikuIndexgenCli.parseArgs(new String[] {
                "--input-directory", "./docs",
                "--input-parent-directory", "./parent"
        }));
        assertThrows(IllegalArgumentException.class, () -> MikuIndexgenCli.parseArgs(new String[] {
                "--input-directory", "./docs",
                "--refresh-index", "./index.json"
        }));
        assertThrows(IllegalArgumentException.class, () -> MikuIndexgenCli.parseArgs(new String[] {
                "--input-parent-directory", "./parent",
                "--refresh-index", "./index.json"
        }));
    }

    @Test
    void runWritesVerboseLogsToStderrAndResultLinesToStdout() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Files.createDirectories(docsDir.resolve("chapter1"));
        Files.write(docsDir.resolve("chapter1").resolve("a.md"), "# A\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--input-directory", docsDir.toString(), "--verbose" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        String stdout = stdoutBuffer.toString("UTF-8");
        String stderr = stderrBuffer.toString("UTF-8");

        assertEquals(0, exitCode);
        assertTrue(stdout.contains("add   : "));
        assertTrue(stdout.contains("completed: 1 subdirectories processed"));
        assertFalse(stdout.contains("verbose: "));
        assertTrue(stderr.contains("verbose: scanning-dir=."));
        assertTrue(stderr.contains("verbose: reading-file=chapter1/a.md"));
    }

    @Test
    void runWritesOutputsUnderOutputDirectoryWhenSpecified() throws Exception {
        Path docsDir = tempDir.resolve("docs");
        Path outDir = tempDir.resolve("out");
        Files.createDirectories(docsDir.resolve("chapter1"));
        Files.write(docsDir.resolve("chapter1").resolve("a.md"), "# A\n".getBytes(StandardCharsets.UTF_8));

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--input-directory", docsDir.toString(), "--output-directory", outDir.toString(), "--markdown" },
                new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"),
                new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"));

        assertEquals(0, exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("index.md")));
        assertFalse(Files.exists(docsDir.resolve("index.json")));
    }

    @Test
    void runProcessesEachDirectChildDirectoryInChildDirectoryBatchMode() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path outDir = tempDir.resolve("out");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Files.createDirectories(child1);
        Files.createDirectories(child2);
        Files.createDirectories(parentDir.resolve(".hidden-child"));
        Files.write(parentDir.resolve("note.md"), "# Parent\n".getBytes(StandardCharsets.UTF_8));
        Files.write(child1.resolve("a.md"), "# A\n".getBytes(StandardCharsets.UTF_8));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--input-parent-directory", parentDir.toString(), "--output-directory", outDir.toString(), "--markdown" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        String stdout = stdoutBuffer.toString("UTF-8");

        assertEquals(0, exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("b1").resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("b1").resolve("index.md")));
        assertTrue(Files.isRegularFile(outDir.resolve("b2").resolve("index.json")));
        assertFalse(Files.exists(outDir.resolve(".hidden-child").resolve("index.json")));
        assertFalse(Files.exists(outDir.resolve("note.md")));
        assertTrue(stdout.contains("completed: 2 child directories processed"));
    }

    @Test
    void runReturnsNonZeroAfterAggregatingChildDirectoryFailures() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path outDir = tempDir.resolve("out");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Path child3 = parentDir.resolve("b3");
        Files.createDirectories(child1);
        Files.createDirectories(child2);
        Files.createDirectories(child3);
        Files.createDirectories(outDir);
        Files.write(child1.resolve("a.md"), "# A\n".getBytes(StandardCharsets.UTF_8));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes(StandardCharsets.UTF_8));
        Files.write(child3.resolve("c.md"), "# C\n".getBytes(StandardCharsets.UTF_8));
        Files.write(outDir.resolve("b2"), "not a directory\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--input-parent-directory", parentDir.toString(), "--output-directory", outDir.toString(), "--markdown" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        String stdout = stdoutBuffer.toString("UTF-8");
        String stderr = stderrBuffer.toString("UTF-8");

        assertEquals(1, exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("b1").resolve("index.json")));
        assertTrue(Files.isRegularFile(outDir.resolve("b3").resolve("index.json")));
        assertTrue(stdout.contains("completed: 3 child directories processed, 1 failed"));
        assertTrue(stderr.contains("failed: "));
        assertTrue(stderr.contains("Output directory must be a directory"));
    }

    @Test
    void runWritesVerboseLogsImmediatelyForChildDirectoryBatchMode() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Path child1 = parentDir.resolve("b1");
        Path child2 = parentDir.resolve("b2");
        Files.createDirectories(child1);
        Files.createDirectories(child2);
        Files.write(child1.resolve("a.md"), "# A\n".getBytes(StandardCharsets.UTF_8));
        Files.write(child2.resolve("b.md"), "# B\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        int exitCode = new MikuIndexgenCli().run(
                new String[] { "--input-parent-directory", parentDir.toString(), "--verbose" },
                new PrintStream(stdoutBuffer, true, "UTF-8"),
                new PrintStream(stderrBuffer, true, "UTF-8"));

        String stdout = stdoutBuffer.toString("UTF-8");
        String stderr = stderrBuffer.toString("UTF-8");

        assertEquals(0, exitCode);
        assertFalse(stdout.contains("verbose: "));
        assertTrue(stderr.contains("verbose: reading-file=a.md"));
        assertTrue(stderr.contains("verbose: reading-file=b.md"));
    }

    private String expectedHelpText() {
        return "Usage:\n"
                + "  miku-indexgen --input-directory <dir> [--output-directory <dir>] [--title \"Docs Index\"] [--markdown] [--no-generator] [--json-summary-path /title,/name] [--no-recursive] [--no-overwrite] [--include-ext md,json] [--exclude-glob \"**/images/**\"] [--input-encoding utf8] [--output-encoding utf8] [--verbose]\n"
                + "  miku-indexgen --input-parent-directory <dir> [--output-directory <dir>] [--title \"Docs Index\"] [--markdown] [--no-generator] [--json-summary-path /title,/name] [--no-recursive] [--no-overwrite] [--include-ext md,json] [--exclude-glob \"**/images/**\"] [--input-encoding utf8] [--output-encoding utf8] [--verbose]\n"
                + "  miku-indexgen --refresh-index <index.json> [--no-overwrite] [--verbose]\n"
                + "\n"
                + "Description:\n"
                + "  Scan a directory and generate index.json. With --markdown, also generate\n"
                + "  index.md. Generated files are artifacts; do not edit them by hand. Rerun\n"
                + "  miku-indexgen or use --refresh-index to update them.\n"
                + "\n"
                + "Default behavior:\n"
                + "  - recursively scans the input directory\n"
                + "  - indexes md,json files by default\n"
                + "  - applies --exclude-glob after extension filtering\n"
                + "  - skips files and directories starting with \".\"\n"
                + "  - writes outputs under the input directory unless --output-directory is set\n"
                + "  - excludes the current run's index.json/index.md from files[]\n"
                + "  - stores generation metadata in index.json for later refresh\n"
                + "\n"
                + "Child-directory batch mode:\n"
                + "  --input-parent-directory processes each direct visible child directory as an\n"
                + "  independent input base. Direct child files are ignored. With a shared\n"
                + "  --output-directory, outputs are written under child-specific directories.\n"
                + "  Child failures are aggregated; remaining children are still processed and\n"
                + "  the command exits non-zero when any child fails.\n"
                + "\n"
                + "Exclude glob:\n"
                + "  --exclude-glob is evaluated against paths relative to the input directory\n"
                + "  after --include-ext. Separators are normalized to \"/\". Matching is\n"
                + "  case-sensitive. Supported glob syntax is only *, ?, and **. Character\n"
                + "  classes, brace expansion, extglob, regular expressions, and OS-dependent\n"
                + "  separators are not supported. Use /** for recursive directory-tree excludes.\n"
                + "\n"
                + "Generated output:\n"
                + "  index.json contains title, generator, generation, basePath, and files[].\n"
                + "  files[] entries include name, path, ext, dir, size, optional Markdown\n"
                + "  metadata, and optional summary.\n"
                + "  files[] is sorted by normalized relative path using UTF-16 code unit order.\n"
                + "  When outputs are written, the CLI reports aligned add   :, update:, or none  :\n"
                + "  labels for each file.\n"
                + "\n"
                + "Markdown:\n"
                + "  - summary is extracted from the first heading or leading body text\n"
                + "  - front matter is parsed as YAML\n"
                + "  - supported fields: title, description, topics, category, status, audience,\n"
                + "    created, updated, sources\n"
                + "  - title, description, and topics are primary scan-time file selection signals\n"
                + "  - category, status, and audience help route which files to read next\n"
                + "  - sources, created, and updated help judge provenance and freshness\n"
                + "  - description is capped at 256 UTF-16 code units and may end with \"...\"\n"
                + "  - unknown fields and unsupported shapes are ignored\n"
                + "\n"
                + "JSON:\n"
                + "  - summary is omitted by default\n"
                + "  - use --json-summary-path /title,/name to extract the first matching string\n"
                + "\n"
                + "Options:\n"
                + "  --input-directory <dir>        Directory to scan.\n"
                + "  --input-parent-directory <dir> Process direct child directories independently.\n"
                + "  --refresh-index <index.json>   Regenerate an existing index from generation metadata.\n"
                + "  --output-directory <dir>       Directory for index.json and optional index.md.\n"
                + "  --title <text>                 Root title in index.json.\n"
                + "  --markdown                     Also generate index.md.\n"
                + "  --no-generator                 Omit root generator metadata.\n"
                + "  --json-summary-path <paths>    Comma-separated JSON Pointer paths.\n"
                + "  --no-recursive                 Scan only immediate files.\n"
                + "  --no-overwrite                 Skip if output already exists.\n"
                + "  --include-ext <exts>           Comma-separated extensions. Default: md,json.\n"
                + "  --exclude-glob <pattern>       Exclude input-relative POSIX paths matching * ? **.\n"
                + "                                 Repeatable. Stored in generation metadata.\n"
                + "  --input-encoding <encoding>    utf8 or shift_jis. Default: utf8.\n"
                + "  --output-encoding <encoding>   utf8 or shift_jis. Default: utf8.\n"
                + "  --verbose                      Print progress and timing details.\n"
                + "  --version                      Print version.\n"
                + "  --help                         Print this help.\n"
                + "\n"
                + "Examples:\n"
                + "  miku-indexgen --input-directory docs\n"
                + "  miku-indexgen --input-directory docs --markdown\n"
                + "  miku-indexgen --input-directory docs --output-directory workplace --markdown\n"
                + "  miku-indexgen --input-parent-directory docs-parent --output-directory out --markdown\n"
                + "  miku-indexgen --input-directory docs --json-summary-path /title,/name\n"
                + "  miku-indexgen --input-directory docs --include-ext md --exclude-glob \"**/images-*/**\" --exclude-glob \"**/images/**\"\n"
                + "  miku-indexgen --refresh-index workplace/index.json\n"
                + "\n"
                + "References:\n"
                + "  docs/input-files-spec.md\n"
                + "  docs/index-json-spec.md\n"
                + "  docs/miku-indexgen-frontmatter-spec.md\n";
    }
}
