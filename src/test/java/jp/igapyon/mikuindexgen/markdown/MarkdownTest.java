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
    void extractFrontMatterExtractsDocumentedYamlMetadataFields() {
        Markdown.MarkdownFrontMatterResult result = Markdown.extractFrontMatter(String.join("\n",
                "---",
                "title: Runtime operations map",
                "description: >",
                "  CLI runtime selection, command examples, and backend policy.",
                "topics: [miku-indexgen, runtime]",
                "category: reference",
                "status: stable",
                "audience:",
                "  - agent",
                "  - maintainer",
                "created: 2026-05-22",
                "updated: 2026-05-23",
                "sources:",
                "  - type: human-input",
                "    label: user-provided requirements",
                "    role: primary",
                "    checked: 2026-05-22",
                "  - type: local-file",
                "    path: docs/index-json-spec.md",
                "    extra: ignored",
                "---",
                "# Body",
                ""));

        assertEquals("Runtime operations map", result.metadata.title);
        assertEquals("CLI runtime selection, command examples, and backend policy.", result.metadata.description);
        assertEquals(java.util.Arrays.asList("miku-indexgen", "runtime"), result.metadata.topics);
        assertEquals("reference", result.metadata.category);
        assertEquals("stable", result.metadata.status);
        assertEquals(java.util.Arrays.asList("agent", "maintainer"), result.metadata.audience);
        assertEquals("2026-05-22", result.metadata.created);
        assertEquals("2026-05-23", result.metadata.updated);
        assertEquals(2, result.metadata.sources.size());
        assertEquals("human-input", result.metadata.sources.get(0).type);
        assertEquals("primary", result.metadata.sources.get(0).role);
        assertEquals("user-provided requirements", result.metadata.sources.get(0).label);
        assertEquals("2026-05-22", result.metadata.sources.get(0).checked);
        assertEquals("local-file", result.metadata.sources.get(1).type);
        assertEquals("docs/index-json-spec.md", result.metadata.sources.get(1).path);
    }

    @Test
    void extractFrontMatterIgnoresUnknownFieldsAndUnsupportedDocumentedValueShapes() {
        Markdown.MarkdownFrontMatterResult result = Markdown.extractFrontMatter(String.join("\n",
                "---",
                "title:",
                "  text: Writing Guide",
                "topics:",
                "  - name: writing",
                "metadata:",
                "  category: reference",
                "sources:",
                "  - label: missing type",
                "---",
                "# Body",
                ""));

        assertNull(result.metadata.title);
        assertNull(result.metadata.topics);
        assertNull(result.metadata.sources);
    }

    @Test
    void extractFrontMatterTreatsInvalidYamlLikeMetadataAsEmptyWhilePreservingBodyExtraction() {
        Markdown.MarkdownFrontMatterResult result = Markdown.extractFrontMatter("---\ntitle: [unterminated\n---\n# Body\n");

        assertEquals("# Body\n", result.body);
        assertNull(result.metadata.title);
        assertNull(result.metadata.topics);
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
