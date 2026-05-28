package jp.igapyon.mikuindexgen.coreapi;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jp.igapyon.mikuindexgen.model.IndexFile;

public class IndexgenResult {
    public int subdirectories;
    public int childDirectoriesProcessed;
    public int childDirectoriesFailed;
    public List<IndexFile> files = new ArrayList<IndexFile>();
    public Path jsonPath;
    public Path markdownPath;
    public Path skippedOutputPath;
    public List<Path> generatedPaths = new ArrayList<Path>();
    public List<String> outputMessages = new ArrayList<String>();
    public List<Path> failedChildDirectories = new ArrayList<Path>();
    public List<String> childFailureMessages = new ArrayList<String>();
    public List<String> logs = new ArrayList<String>();
    public IndexgenTimings timings = new IndexgenTimings();

    public boolean skipped() {
        return skippedOutputPath != null;
    }

    public boolean failed() {
        return childDirectoriesFailed > 0;
    }
}
