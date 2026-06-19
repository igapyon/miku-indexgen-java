package jp.igapyon.mikuindexgen.coreapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class ExcludeGlobTest {
    @Test
    void matchesStarAndQuestionWithinOnePathSegment() {
        assertTrue(ExcludeGlob.matchesExcludeGlob("notes/image-prompt.md", "notes/image-*.md"));
        assertTrue(ExcludeGlob.matchesExcludeGlob("notes/image-prompt.md", "notes/image-??????.md"));
        assertFalse(ExcludeGlob.matchesExcludeGlob("notes/nested/image-prompt.md", "notes/*.md"));
    }

    @Test
    void matchesDoubleStarAcrossZeroOrMorePathSegments() {
        assertTrue(ExcludeGlob.matchesExcludeGlob("note-image-recovery.md", "**/note-image-recovery.md"));
        assertTrue(ExcludeGlob.matchesExcludeGlob("2026/05/note-image-recovery.md", "**/note-image-recovery.md"));
        assertTrue(ExcludeGlob.matchesExcludeGlob("2026/05/images/foo.md", "**/images/*"));
        assertFalse(ExcludeGlob.matchesExcludeGlob("2026/05/images/sections/foo.md", "**/images/*"));
    }

    @Test
    void matchesAnyConfiguredExcludeGlob() {
        assertTrue(ExcludeGlob.matchesAnyExcludeGlob("2026/05/images-ai-native/src/sections/001/section-text.md",
                Arrays.asList("**/images-*/*", "**/section-text.md")));
        assertFalse(ExcludeGlob.matchesAnyExcludeGlob("2026/05/article.md",
                Arrays.asList("**/images/*", "**/section-text.md")));
    }

    @Test
    void normalizesSeparatorsWhitespaceDuplicatesAndEmptyPatterns() {
        assertEquals(Arrays.asList("**/images/*"),
                ExcludeGlob.normalizeExcludeGlobPatterns(Arrays.asList(" **\\images\\* ", "", "**/images/*")));
    }
}
