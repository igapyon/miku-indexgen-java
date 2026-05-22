package jp.igapyon.mikuindexgen.model;

import java.util.List;

public class GenerationMetadata {
    public int schemaVersion = 1;
    public String inputPath;
    public boolean markdownOutput;
    public boolean recursive;
    public List<String> includeExtensions;
    public String inputEncoding;
    public String outputEncoding;
    public List<String> jsonSummaryPaths;
    public String title;
    public boolean includeGeneratorMetadata;
}
