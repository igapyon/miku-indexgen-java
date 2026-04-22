package jp.igapyon.mikuindexgen.pathutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PathUtilsTest {
    @Test
    void getFileExtensionReturnsTheLowerCasedExtensionForRegularFileNames() {
        assertEquals("md", PathUtils.getFileExtension("/tmp/Example.MD"));
        assertEquals("gz", PathUtils.getFileExtension("dir/archive.tar.gz"));
    }

    @Test
    void getFileExtensionReturnsAnEmptyStringForFilesWithoutAUsableExtension() {
        assertEquals("", PathUtils.getFileExtension("README"));
        assertEquals("", PathUtils.getFileExtension(".gitignore"));
        assertEquals("", PathUtils.getFileExtension("note."));
    }

    @Test
    void getFileNameReturnsTheBasenamePortionOfAPath() {
        assertEquals("a.md", PathUtils.getFileName("/tmp/docs/a.md"));
    }

    @Test
    void toPosixPathNormalizesWindowsSeparators() {
        assertEquals("dir/child/file.md", PathUtils.toPosixPath("dir\\child\\file.md"));
    }
}
