package jp.igapyon.mikuindexgen.coreapi;

import java.nio.file.Path;

final class OutputPaths {
    final Path jsonPath;
    final Path markdownPath;

    OutputPaths(Path jsonPath, Path markdownPath) {
        this.jsonPath = jsonPath;
        this.markdownPath = markdownPath;
    }
}
