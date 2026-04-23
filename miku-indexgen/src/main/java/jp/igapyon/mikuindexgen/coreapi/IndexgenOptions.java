package jp.igapyon.mikuindexgen.coreapi;

import java.util.ArrayList;
import java.util.List;

import jp.igapyon.mikuindexgen.model.CliOptions;

public class IndexgenOptions {
    public String inputDirectory;
    public String outputFileName = "index.json";
    public String title;
    public boolean markdownOutput;
    public Boolean includeGeneratorMetadata = Boolean.TRUE;
    public List<String> jsonSummaryPaths;
    public boolean recursive = true;
    public boolean overwrite = true;
    public boolean verbose;
    public List<String> includeExtensions;
    public String inputEncoding = "utf8";
    public String outputEncoding = "utf8";

    public IndexgenOptions() {
        includeExtensions = new ArrayList<String>();
        includeExtensions.add("md");
        includeExtensions.add("json");
    }

    public static IndexgenOptions fromCliOptions(CliOptions cliOptions) {
        IndexgenOptions options = new IndexgenOptions();
        options.inputDirectory = cliOptions.inputDirectory;
        options.outputFileName = cliOptions.outputFileName;
        options.title = cliOptions.title;
        options.markdownOutput = cliOptions.markdownOutput;
        options.includeGeneratorMetadata = cliOptions.includeGeneratorMetadata;
        options.jsonSummaryPaths = copyList(cliOptions.jsonSummaryPaths);
        options.recursive = cliOptions.recursive;
        options.overwrite = cliOptions.overwrite;
        options.verbose = cliOptions.verbose;
        options.includeExtensions = copyList(cliOptions.includeExtensions);
        options.inputEncoding = cliOptions.inputEncoding;
        options.outputEncoding = cliOptions.outputEncoding;
        return options;
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }
}
