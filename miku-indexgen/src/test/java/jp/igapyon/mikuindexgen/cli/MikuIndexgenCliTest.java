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
        assertEquals("shift_jis", options.inputEncoding);
        assertEquals("shift_jis", options.outputEncoding);
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
        assertEquals("miku-indexgen 1.2.0\n", stdoutBuffer.toString("UTF-8"));
        assertEquals("", stderrBuffer.toString("UTF-8"));
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
    void parseArgsRejectsUsingInputDirectoryAndInputParentDirectoryTogether() {
        assertThrows(IllegalArgumentException.class, () -> MikuIndexgenCli.parseArgs(new String[] {
                "--input-directory", "./docs",
                "--input-parent-directory", "./parent"
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
        assertTrue(stdout.contains("generated: "));
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
}
