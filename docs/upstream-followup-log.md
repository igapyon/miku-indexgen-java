# Upstream Follow-up Log

## Initial Conversion Start

```text
upstream file:
  workplace/miku-indexgen/src/markdown.ts

java classes:
  jp.igapyon.mikuindexgen.markdown.Markdown

tests:
  jp.igapyon.mikuindexgen.markdown.MarkdownTest

diff summary:
  behavior diff:
    - No known intentional behavior difference in the covered helper methods.
  naming diff:
    - Java class name uses UpperCamelCase.
  unmigrated diff:
    - None known for the covered helper methods.
  Java-side extension:
    - None.

follow-up:
  - `2026-04-22`: `mvn test` passed.
```

```text
upstream file:
  workplace/miku-indexgen/src/json-summary.ts

java classes:
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummary
  jp.igapyon.mikuindexgen.jsonsummary.Parser

tests:
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummaryTest

diff summary:
  behavior diff:
    - No known intentional behavior difference in the covered JSON summary cases.
  naming diff:
    - Java package uses jsonsummary.
  unmigrated diff:
    - Full JSON behavior beyond upstream test intent remains to be checked.
  Java-side extension:
    - Parser is a Java-side helper.

follow-up:
  - `2026-04-22`: `mvn test` passed.
  - Confirm indexer JSON summary integration after indexer conversion.
```
