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

```text
upstream file:
  workplace/miku-indexgen/src/indexer.ts

java classes:
  jp.igapyon.mikuindexgen.coreapi.Indexgen
  jp.igapyon.mikuindexgen.coreapi.IndexgenOptions
  jp.igapyon.mikuindexgen.coreapi.IndexgenResult
  jp.igapyon.mikuindexgen.coreapi.IndexgenTimings

tests:
  jp.igapyon.mikuindexgen.coreapi.IndexgenTest

diff summary:
  behavior diff:
    - No known intentional behavior difference in the covered indexer cases.
  naming diff:
    - Java core API uses IndexgenOptions / IndexgenResult so CLI and Maven plugin can share the same contract.
  unmigrated diff:
    - No known unmigrated diff in the covered CLI / plugin adapter contract.
  Java-side extension:
    - IndexgenResult returns generated paths, skipped path, logs, and timings for adapter layers.

follow-up:
  - `2026-04-22`: `mvn test` passed.
  - `2026-04-22`: `mvn package` passed.
  - `2026-04-22`: jar smoke passed with Markdown output enabled.
  - `2026-04-22`: Maven plugin full-coordinate smoke passed.
  - Short prefix execution requires Maven plugin prefix resolution for the `jp.igapyon` plugin group.
  - `2026-05-08`: fetched upstream `miku-indexgen` and followed `formatIndexJson` behavior from `origin/devel` so `index.json` file entries are emitted as one-line records.
  - `2026-05-08`: `mvn test` passed.
```

```text
upstream file:
  workplace/miku-indexgen/src/logging.ts

java classes:
  jp.igapyon.mikuindexgen.logging.Logging
  jp.igapyon.mikuindexgen.logging.VerboseLogger

tests:
  jp.igapyon.mikuindexgen.coreapi.IndexgenTest

diff summary:
  behavior diff:
    - Verbose log text is covered through IndexgenResult logs.
  naming diff:
    - Java logging helper is package-scoped by responsibility rather than TS export style.
  unmigrated diff:
    - None known for covered verbose output.
  Java-side extension:
    - Logs are accumulated for CLI / Maven plugin adapters instead of being written directly by core.

follow-up:
  - `2026-04-22`: `mvn test` passed.
```

```text
upstream file:
  Java-side Maven integration

java classes:
  jp.igapyon.mikuindexgen.mavenplugin.MikuIndexgenMojo

tests:
  jp.igapyon.mikuindexgen.mavenplugin.MikuIndexgenMojoTest

diff summary:
  behavior diff:
    - This is a Java-side execution adapter, not an upstream TypeScript feature.
  naming diff:
    - Maven goal name is `index`.
  unmigrated diff:
    - Short prefix usage needs plugin prefix resolution documentation.
  Java-side extension:
    - Adds `miku-indexgen-maven-plugin` module with `packaging=maven-plugin`.

follow-up:
  - `2026-04-22`: `mvn test` passed in the multi-module reactor.
  - `2026-04-22`: `mvn package` passed in the multi-module reactor.
  - `2026-04-22`: full-coordinate Maven plugin smoke passed.
```
