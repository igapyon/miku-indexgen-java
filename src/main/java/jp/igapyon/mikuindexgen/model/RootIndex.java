package jp.igapyon.mikuindexgen.model;

import java.util.List;

public class RootIndex {
    public String title;
    public String generator;
    public GenerationMetadata generation;
    public String basePath;
    public List<IndexFile> files;
}
