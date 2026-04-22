# Upstream Class Mapping

This document tracks `upstream file -> Java class` mapping for the miku-indexgen straight conversion.

## Source Mapping

```text
upstream file:
  workplace/miku-indexgen/src/types.ts

java classes:
  jp.igapyon.mikuindexgen.model.CliOptions
  jp.igapyon.mikuindexgen.model.IndexFile
  jp.igapyon.mikuindexgen.model.RootIndex

notes:
  - POJO classes use public fields during the initial straight conversion.
```

```text
upstream file:
  workplace/miku-indexgen/src/markdown.ts

java classes:
  jp.igapyon.mikuindexgen.markdown.Markdown

notes:
  - Initial conversion covers sanitizeTextForIndex, escapeMarkdownTableCell, extractSummary, and buildMarkdownIndexContent.
```

```text
upstream file:
  workplace/miku-indexgen/src/json-summary.ts

java classes:
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummary
  jp.igapyon.mikuindexgen.jsonsummary.Parser

notes:
  - Parser is a Java-side helper for the JSON Pointer summary use case.
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

notes:
  - Initial conversion covers parseArgs, parseIncludeExtensions, and printHelp.
  - Runtime index generation command is not implemented yet.
```

```text
upstream file:
  workplace/miku-indexgen/src/indexer.ts

java classes:
  pending

notes:
  - Next core implementation target.
```

```text
upstream file:
  workplace/miku-indexgen/src/logging.ts

java classes:
  pending

notes:
  - To be implemented with indexer runtime behavior.
```

```text
upstream file:
  workplace/miku-indexgen/src/main.ts

java classes:
  jp.igapyon.mikuindexgen.cli.MikuIndexgenCli

notes:
  - CLI main entrypoint exists.
  - Public facade exports are not complete yet.
```
