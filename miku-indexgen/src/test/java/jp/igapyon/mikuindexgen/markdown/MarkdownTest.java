package jp.igapyon.mikuindexgen.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MarkdownTest {
    @Test
    void extractSummaryUsesTheFirstHeadingWhenTheFirstNonEmptyLineStartsWithHash() {
        assertEquals("Title", Markdown.extractSummary("\n## Title\nBody"));
    }

    @Test
    void extractSummaryUsesBodyTextUntilTheFirstHeadingWhenTheFileStartsWithoutAHeading() {
        assertEquals("Workbook: sample.xlsx Second line",
                Markdown.extractSummary("Workbook: sample.xlsx\nSecond line\n# Heading\nBody"));
    }

    @Test
    void extractSummaryLimitsBodyDerivedSummaryTo256Characters() {
        assertEquals(repeat("a", 256), Markdown.extractSummary(repeat("a", 300) + "\n"));
    }

    @Test
    void extractSummaryIgnoresFrontMatter() {
        assertEquals("Body Title",
                Markdown.extractSummary("---\ntitle: Front Matter Title\ntopics:\n  - writing\n---\n\n# Body Title\n"));
    }

    @Test
    void extractSummaryReturnsNullForBlankInput() {
        assertNull(Markdown.extractSummary("\n\n"));
    }

    @Test
    void extractFrontMatterExtractsTitleAndTopics() {
        Markdown.MarkdownFrontMatterResult result = Markdown
                .extractFrontMatter("---\ntitle: Writing Guide\ntopics:\n  - writing\n  - article\n---\n\n# Body\n");

        assertEquals("# Body\n", result.body);
        assertEquals("Writing Guide", result.metadata.title);
        assertEquals(java.util.Arrays.asList("writing", "article"), result.metadata.topics);
    }

    @Test
    void extractFrontMatterExtractsQuotedTitleAndInlineTopics() {
        Markdown.MarkdownFrontMatterResult result = Markdown
                .extractFrontMatter("---\ntitle: \"Writing Guide\"\ntopics: [writing, \"article\"]\n---\n# Body\n");

        assertEquals("Writing Guide", result.metadata.title);
        assertEquals(java.util.Arrays.asList("writing", "article"), result.metadata.topics);
    }

    @Test
    void extractFrontMatterTreatsUnclosedFrontMatterAsBodyText() {
        String markdown = "---\ntitle: Writing Guide\n# Body\n";
        Markdown.MarkdownFrontMatterResult result = Markdown.extractFrontMatter(markdown);

        assertEquals(markdown, result.body);
        assertNull(result.metadata.title);
        assertNull(result.metadata.topics);
    }

    @Test
    void sanitizeTextForIndexReplacesProblematicCharactersAndNormalizesWhitespace() {
        assertEquals("A B C D E", Markdown.sanitizeTextForIndex("A\tB\nC\u200BD\u0007E"));
    }

    @Test
    void escapeMarkdownTableCellEscapesPipeCharactersAndBackslashesForMarkdownTables() {
        assertEquals("A\\|B\\\\C", Markdown.escapeMarkdownTableCell("A|B\\C"));
    }

    @Test
    void escapeMarkdownTableCellEscapesHtmlSensitiveCharactersForMarkdownOutput() {
        assertEquals("&lt;a&amp;b&gt;", Markdown.escapeMarkdownTableCell("<a&b>"));
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
