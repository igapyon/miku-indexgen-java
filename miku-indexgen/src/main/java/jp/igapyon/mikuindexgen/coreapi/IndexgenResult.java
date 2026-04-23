package jp.igapyon.mikuindexgen.coreapi;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jp.igapyon.mikuindexgen.model.IndexFile;

public class IndexgenResult {
    public int subdirectories;
    public int childDirectoriesProcessed;
    public List<IndexFile> files = new ArrayList<IndexFile>();
    public Path jsonPath;
    public Path markdownPath;
    public Path skippedOutputPath;
    public List<Path> generatedPaths = new ArrayList<Path>();
    public List<String> logs = new ArrayList<String>();
    public IndexgenTimings timings = new IndexgenTimings();

    public boolean skipped() {
        return skippedOutputPath != null;
    }
}
