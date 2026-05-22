# Upstream Class Mapping

This document tracks `upstream file -> Java class` mapping for the miku-indexgen straight conversion.

## Source Mapping

```text
upstream file:
  workplace/miku-indexgen/src/types.ts

java classes:
  jp.igapyon.mikuindexgen.model.CliOptions
  jp.igapyon.mikuindexgen.model.GenerationMetadata
  jp.igapyon.mikuindexgen.model.IndexFile
  jp.igapyon.mikuindexgen.model.IndexSource
  jp.igapyon.mikuindexgen.model.RootIndex

notes:
  - POJO classes use public fields during the initial straight conversion.
```

```text
upstream file:
  workplace/miku-indexgen/src/markdown.ts
  workplace/miku-indexgen/src/frontmatter.ts
  workplace/miku-indexgen/src/text-sanitize.ts

java classes:
  jp.igapyon.mikuindexgen.markdown.MarkdownFrontMatterParser
  jp.igapyon.mikuindexgen.markdown.Markdown

notes:
  - Initial conversion covers sanitizeTextForIndex, escapeMarkdownTableCell, extractSummary, front matter extraction, and buildMarkdownIndexContent.
  - Java front matter parsing covers the documented scalar, string-array, date-only, and source metadata shapes used by upstream tests.
```

```text
upstream file:
  workplace/miku-indexgen/src/json-summary.ts

java classes:
  jp.igapyon.mikuindexgen.json.JsonParser
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummary

notes:
  - JsonParser is a Java-side helper shared by JSON summary extraction and generation metadata refresh.
```

```text
upstream file:
  workplace/miku-indexgen/src/path-utils.ts

java classes:
  jp.igapyon.mikuindexgen.pathutils.PathUtils

notes:
  - Initial conversion covers toPosixPath, getFileExtension, and getFileName.
```

```text
upstream file:
  workplace/miku-indexgen/src/encoding.ts

java classes:
  jp.igapyon.mikuindexgen.encoding.Encoding

notes:
  - Initial conversion covers utf8 and shift_jis aliases using Java Charset.
```

```text
upstream file:
  workplace/miku-indexgen/src/cli.ts

java classes:
  jp.igapyon.mikuindexgen.cli.MikuIndexgenCli
  jp.igapyon.mikuindexgen.cli.HelpRequestedException
  jp.igapyon.mikuindexgen.coreapi.IndexgenOptions

notes:
  - Initial conversion covers parseArgs, parseIncludeExtensions, and printHelp.
  - CLI converts parsed arguments into core IndexgenOptions.
```

```text
upstream file:
  workplace/miku-indexgen/src/indexer.ts
  workplace/miku-indexgen/src/generation.ts
  workplace/miku-indexgen/src/index-json.ts

java classes:
  jp.igapyon.mikuindexgen.coreapi.IndexJsonFormatter
  jp.igapyon.mikuindexgen.coreapi.Generation
  jp.igapyon.mikuindexgen.coreapi.Indexgen
  jp.igapyon.mikuindexgen.coreapi.IndexgenOptions
  jp.igapyon.mikuindexgen.coreapi.IndexgenResult
  jp.igapyon.mikuindexgen.coreapi.IndexgenTimings

notes:
  - Core API is intentionally independent of CLI argv parsing.
  - Separated adapters can call Indexgen.createIndexes(IndexgenOptions).
  - JSON output formatting follows upstream `formatIndexJson` so each file entry is emitted as one search-friendly line.
```

```text
upstream file:
  Java-side Maven integration

java classes:
  separated repository concern

notes:
  - Java-side extension.
  - Maven plugin implementation is maintained in `miku-indexgen-java-maven`.
  - The plugin should call Indexgen.createIndexes(IndexgenOptions) from the runtime artifact.
```

```text
upstream file:
  workplace/miku-indexgen/src/logging.ts

java classes:
  jp.igapyon.mikuindexgen.logging.Logging
  jp.igapyon.mikuindexgen.logging.VerboseLogger

notes:
  - Verbose logs are accumulated in IndexgenResult for CLI / Maven plugin adapters.
```

```text
upstream file:
  workplace/miku-indexgen/src/main.ts

java classes:
  jp.igapyon.mikuindexgen.cli.MikuIndexgenCli

notes:
  - CLI main entrypoint exists.
  - Public facade exports are represented by focused Java classes rather than a single export file.
```
