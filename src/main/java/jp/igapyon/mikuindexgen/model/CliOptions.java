package jp.igapyon.mikuindexgen.model;

import java.util.List;

public class CliOptions {
    public String targetDir;
    public String outputFileName;
    public String title;
    public boolean markdownOutput;
    public Boolean includeGeneratorMetadata;
    public List<String> jsonSummaryPaths;
    public boolean recursive;
    public boolean overwrite;
    public boolean verbose;
    public List<String> includeExtensions;
    public String inputEncoding;
    public String outputEncoding;
}
