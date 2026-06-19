package jp.igapyon.mikuindexgen.model;

import java.util.List;

public class CliOptions {
    public String inputDirectory;
    public String inputParentDirectory;
    public String outputDirectory;
    public String refreshIndex;
    public String title;
    public boolean markdownOutput;
    public Boolean includeGeneratorMetadata;
    public List<String> jsonSummaryPaths;
    public boolean recursive;
    public boolean overwrite;
    public boolean verbose;
    public List<String> includeExtensions;
    public List<String> excludeGlobs;
    public String inputEncoding;
    public String outputEncoding;
}
