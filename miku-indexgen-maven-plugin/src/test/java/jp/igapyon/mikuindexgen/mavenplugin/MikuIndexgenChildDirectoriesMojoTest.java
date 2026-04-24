package jp.igapyon.mikuindexgen.mavenplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;

class MikuIndexgenChildDirectoriesMojoTest {
    @TempDir
    Path tempDir;

    @Test
    void toOptionsMapsMavenParametersToCoreOptions() {
        MikuIndexgenChildDirectoriesMojo mojo = new MikuIndexgenChildDirectoriesMojo();
        mojo.setInputParentDirectory(tempDir.toFile());
        mojo.setOutputDirectory(tempDir.resolve("out").toFile());
        mojo.setTitle("Docs Index");
        mojo.setMarkdown(true);
        mojo.setIncludeGeneratorMetadata(false);
        mojo.setJsonSummaryPaths(Arrays.asList("/title", "/name"));
        mojo.setRecursive(false);
        mojo.setOverwrite(false);
        mojo.setVerbose(true);
        mojo.setIncludeExtensions(Arrays.asList("md"));
        mojo.setInputEncoding("utf8");
        mojo.setOutputEncoding("utf8");

        IndexgenOptions options = mojo.toOptions();

        assertEquals(tempDir.toString(), options.inputParentDirectory);
        assertEquals(tempDir.resolve("out").toString(), options.outputDirectory);
        assertEquals("Docs Index", options.title);
        assertTrue(options.markdownOutput);
        assertEquals(Boolean.FALSE, options.includeGeneratorMetadata);
        assertEquals(Arrays.asList("/title", "/name"), options.jsonSummaryPaths);
        assertEquals(false, options.recursive);
        assertEquals(false, options.overwrite);
        assertTrue(options.verbose);
        assertEquals(Arrays.asList("md"), options.includeExtensions);
        assertEquals("utf8", options.inputEncoding);
        assertEquals("utf8", options.outputEncoding);
    }

    @Test
    void executeGeneratesIndexFilesForEachDirectChildDirectory() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Files.createDirectories(parentDir.resolve("b1"));
        Files.createDirectories(parentDir.resolve("b2"));
        Files.write(parentDir.resolve("b1").resolve("sample.md"), "# Sample\n".getBytes("UTF-8"));
        Files.write(parentDir.resolve("b2").resolve("other.md"), "# Other\n".getBytes("UTF-8"));

        MikuIndexgenChildDirectoriesMojo mojo = new MikuIndexgenChildDirectoriesMojo();
        mojo.setInputParentDirectory(parentDir.toFile());
        mojo.setOutputDirectory(tempDir.resolve("out").toFile());
        mojo.setMarkdown(true);
        mojo.execute();

        assertTrue(Files.isRegularFile(tempDir.resolve("out").resolve("b1").resolve("index.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("out").resolve("b1").resolve("index.md")));
        assertTrue(Files.isRegularFile(tempDir.resolve("out").resolve("b2").resolve("index.json")));
    }

    @Test
    void executeWritesVerboseLogsThroughMojoLoggerForEachChildDirectoryWithoutDuplicatingBufferedLogs() throws Exception {
        Path parentDir = tempDir.resolve("parent");
        Files.createDirectories(parentDir.resolve("b1"));
        Files.createDirectories(parentDir.resolve("b2"));
        Files.write(parentDir.resolve("b1").resolve("sample.md"), "# Sample\n".getBytes("UTF-8"));
        Files.write(parentDir.resolve("b2").resolve("other.md"), "# Other\n".getBytes("UTF-8"));

        RecordingLog log = new RecordingLog();
        MikuIndexgenChildDirectoriesMojo mojo = new MikuIndexgenChildDirectoriesMojo();
        mojo.setLog(log);
        mojo.setInputParentDirectory(parentDir.toFile());
        mojo.setVerbose(true);
        mojo.execute();

        assertEquals(1, log.countInfoLine("verbose: reading-file=sample.md"));
        assertEquals(1, log.countInfoLine("verbose: reading-file=other.md"));
    }
}
