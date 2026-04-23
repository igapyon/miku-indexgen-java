package jp.igapyon.mikuindexgen.mavenplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;

class MikuIndexgenMojoTest {
    @TempDir
    Path tempDir;

    @Test
    void toOptionsMapsMavenParametersToCoreOptions() {
        MikuIndexgenMojo mojo = new MikuIndexgenMojo();
        mojo.setInputDirectory(tempDir.toFile());
        mojo.setOutputFileName("SUMMARY.json");
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

        assertEquals(tempDir.toString(), options.inputDirectory);
        assertEquals("SUMMARY.json", options.outputFileName);
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
    void executeGeneratesIndexFiles() throws Exception {
        Files.write(tempDir.resolve("sample.md"), "# Sample\n".getBytes("UTF-8"));

        MikuIndexgenMojo mojo = new MikuIndexgenMojo();
        mojo.setInputDirectory(tempDir.toFile());
        mojo.setMarkdown(true);
        mojo.execute();

        assertTrue(Files.isRegularFile(tempDir.resolve("index.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("index.md")));
        assertTrue(new String(Files.readAllBytes(tempDir.resolve("index.json")), "UTF-8").contains("\"summary\": \"Sample\""));
    }
}
