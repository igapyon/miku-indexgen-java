package jp.igapyon.mikuindexgen.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuindexgen.model.CliOptions;

class MikuIndexgenCliTest {
    @Test
    void parseArgsParsesTheTargetDirAndOptions() {
        CliOptions options = MikuIndexgenCli.parseArgs(new String[] {
                "./docs",
                "--output", "SUMMARY.json",
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

        assertEquals("./docs", options.targetDir);
        assertEquals("SUMMARY.json", options.outputFileName);
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
        assertTrue(MikuIndexgenCli.parseArgs(new String[] { "./docs" }).includeGeneratorMetadata.booleanValue());
    }

    @Test
    void parseIncludeExtensionsNormalizesACommaSeparatedExtensionList() {
        assertEquals(Arrays.asList("md", "json"), MikuIndexgenCli.parseIncludeExtensions(".MD, json ,md"));
    }

    @Test
    void parseArgsSignalsHelpRequestsWithoutExitingFromTheParser() {
        assertThrows(HelpRequestedException.class, () -> MikuIndexgenCli.parseArgs(new String[] { "--help" }));
    }
}
