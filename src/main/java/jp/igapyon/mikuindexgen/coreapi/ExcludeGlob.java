package jp.igapyon.mikuindexgen.coreapi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ExcludeGlob {
    private ExcludeGlob() {
    }

    public static String normalizeExcludeGlobPattern(String pattern) {
        if (pattern == null) {
            return "";
        }
        String normalized = pattern.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public static List<String> normalizeExcludeGlobPatterns(List<String> patterns) {
        LinkedHashSet<String> normalizedPatterns = new LinkedHashSet<String>();
        if (patterns == null) {
            return new ArrayList<String>();
        }
        for (String pattern : patterns) {
            String normalized = normalizeExcludeGlobPattern(pattern == null ? "" : pattern.trim());
            if (normalized.length() > 0) {
                normalizedPatterns.add(normalized);
            }
        }
        return new ArrayList<String>(normalizedPatterns);
    }

    public static boolean matchesAnyExcludeGlob(String relativePath, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        String normalizedPath = normalizeExcludeGlobPattern(relativePath);
        for (String pattern : patterns) {
            if (matchesExcludeGlob(normalizedPath, pattern)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesExcludeGlob(String relativePath, String pattern) {
        String[] pathSegments = splitSegments(normalizeExcludeGlobPattern(relativePath));
        String[] patternSegments = splitSegments(normalizeExcludeGlobPattern(pattern));
        return matchSegments(pathSegments, patternSegments, 0, 0);
    }

    private static String[] splitSegments(String value) {
        if (value == null || value.length() == 0) {
            return new String[0];
        }
        String[] rawSegments = value.split("/");
        List<String> segments = new ArrayList<String>();
        for (String segment : rawSegments) {
            if (segment.length() > 0) {
                segments.add(segment);
            }
        }
        return segments.toArray(new String[segments.size()]);
    }

    private static boolean matchSegments(String[] pathSegments, String[] patternSegments, int pathIndex,
            int patternIndex) {
        if (patternIndex == patternSegments.length) {
            return pathIndex == pathSegments.length;
        }

        String patternSegment = patternSegments[patternIndex];
        if ("**".equals(patternSegment)) {
            if (matchSegments(pathSegments, patternSegments, pathIndex, patternIndex + 1)) {
                return true;
            }
            return pathIndex < pathSegments.length && matchSegments(pathSegments, patternSegments, pathIndex + 1,
                    patternIndex);
        }

        return pathIndex < pathSegments.length
                && matchesPathSegment(pathSegments[pathIndex], patternSegment)
                && matchSegments(pathSegments, patternSegments, pathIndex + 1, patternIndex + 1);
    }

    private static boolean matchesPathSegment(String pathSegment, String patternSegment) {
        return matchSegmentChars(pathSegment, patternSegment, 0, 0);
    }

    private static boolean matchSegmentChars(String value, String pattern, int valueIndex, int patternIndex) {
        if (patternIndex == pattern.length()) {
            return valueIndex == value.length();
        }

        char patternChar = pattern.charAt(patternIndex);
        if (patternChar == '*') {
            if (matchSegmentChars(value, pattern, valueIndex, patternIndex + 1)) {
                return true;
            }
            return valueIndex < value.length() && matchSegmentChars(value, pattern, valueIndex + 1, patternIndex);
        }

        if (patternChar == '?') {
            return valueIndex < value.length() && matchSegmentChars(value, pattern, valueIndex + 1, patternIndex + 1);
        }

        return valueIndex < value.length()
                && value.charAt(valueIndex) == patternChar
                && matchSegmentChars(value, pattern, valueIndex + 1, patternIndex + 1);
    }
}
