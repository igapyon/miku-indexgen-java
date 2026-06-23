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
  jp.igapyon.mikuindexgen.json.JsonParser
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummary

tests:
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummaryTest

diff summary:
  behavior diff:
    - No known intentional behavior difference in the covered JSON summary cases.
  naming diff:
    - Java package uses jsonsummary for summary behavior and json for shared parsing.
  unmigrated diff:
    - Full JSON behavior beyond upstream test intent remains to be checked.
  Java-side extension:
    - JsonParser is a Java-side helper shared by JSON summary extraction and generation metadata refresh.

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
  - `2026-05-16`: fetched upstream `miku-indexgen` and followed Markdown front matter `title` / `topics` extraction from `origin/devel` through `10efa60`.
  - `2026-05-16`: added Java CLI `--version` / `-v` handling to follow upstream `v1.2.0`.
  - `2026-05-22`: fetched upstream `miku-indexgen` and followed `origin/devel` through `07064d0` / upstream `1.3.0`.
  - `2026-05-22`: added Java generation metadata output, `--refresh-index`, and documented Markdown front matter metadata fields.
  - `2026-05-22`: Java version updated to `1.3.0`.
  - `2026-05-22`: copied upstream product-specific docs `input-files-spec.md`, `index-json-spec.md`, and `miku-indexgen-frontmatter-spec.md`; replaced copied shared `miku-soft-*` docs with `docs/miku-soft-reference.md`.
  - `2026-05-22`: followed up separated `miku-indexgen-java-maven` for runtime `1.3.0`; plugin tests, package, and smoke passed after local runtime install.
  - `2026-05-22`: `mvn test` passed.
  - `2026-05-29`: fetched upstream `miku-indexgen` and followed `origin/devel` through `1ac90d0` / upstream `1.4.4`.
  - `2026-05-29`: Java CLI output status labels now follow upstream `add   :`, `update:`, and `none  :` behavior.
  - `2026-05-29`: Java version updated to `1.4.4`.
  - `2026-06-06`: fetched upstream `miku-indexgen` and followed `origin/devel` through `b82d542` / upstream `1.5.0`.
  - `2026-06-06`: Java output ordering now follows upstream UTF-16 code unit order for POSIX-style relative paths.
  - `2026-06-06`: Java Markdown front matter `description` metadata is shortened to 256 UTF-16 code units, including the trailing `...`.
  - `2026-06-06`: Java version updated to `1.5.0`.
  - `2026-06-07`: fetched upstream `miku-indexgen` and followed `origin/devel` through `40539f4` / upstream `1.5.1`.
  - `2026-06-07`: Java CLI help and README now describe index metadata field roles added upstream.
  - `2026-06-07`: Java version updated to `1.5.1`.
  - `2026-06-20`: fetched upstream `miku-indexgen` and followed `origin/devel` through `e170844` / upstream `1.6.0`.
  - `2026-06-20`: Java runtime now supports repeatable `--exclude-glob` with `*`, `?`, and `**` matching against input-relative POSIX paths.
  - `2026-06-20`: Java generation metadata now stores `excludeGlobs` so `--refresh-index` keeps the same exclusion rules.
  - `2026-06-20`: Java version updated to `1.6.0`.
  - `2026-06-23`: fetched upstream `miku-indexgen` and followed `origin/devel` through `daf1e2b` / upstream `1.6.2`.
  - `2026-06-23`: upstream `1.6.1` child-directory batch mode matched the existing Java `--input-parent-directory` runtime contract.
  - `2026-06-23`: Java docs now include upstream `1.6.2` Markdown front matter examples under `docs/examples/`.
  - `2026-06-23`: Java version updated to `1.6.2`.
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
  separated repository concern

tests:
  separated repository concern

diff summary:
  behavior diff:
    - This is a Java-side execution adapter, not an upstream TypeScript feature.
  naming diff:
    - Maven goal name is `index`.
  unmigrated diff:
    - Short prefix usage needs plugin prefix resolution documentation.
  Java-side extension:
    - Maven plugin implementation is maintained in `miku-indexgen-java-maven`.

follow-up:
  - `2026-04-22`: `mvn test` passed in the multi-module reactor.
  - `2026-04-22`: `mvn package` passed in the multi-module reactor.
  - `2026-04-22`: full-coordinate Maven plugin smoke passed.
  - `2026-05-16`: Maven plugin implementation was separated to `miku-indexgen-java-maven`; this runtime repository now records Maven plugin checks as separated-repository concerns.
```
