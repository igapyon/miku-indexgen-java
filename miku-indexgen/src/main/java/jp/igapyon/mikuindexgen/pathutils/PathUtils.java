package jp.igapyon.mikuindexgen.pathutils;

import java.io.File;

public final class PathUtils {
    private PathUtils() {
    }

    public static String toPosixPath(String path) {
        return path.replace('\\', '/');
    }

    public static String getFileExtension(String filePath) {
        String fileName = getFileName(filePath);
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    public static String getFileName(String filePath) {
        return new File(filePath).getName();
    }
}
