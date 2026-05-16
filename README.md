# miku-indexgen-java

`miku-indexgen-java` is a Java implementation of `miku-indexgen`.

It scans a directory and generates `index.json`.
When Markdown output is enabled, it also generates `index.md`.

## Download

Download the runtime jar from the GitHub Releases page.

Runtime artifact:

- `miku-indexgen-<version>.jar`

## Quick Start

Generate an index from one directory:

```bash
java -jar miku-indexgen-<version>.jar \
  --input-directory docs \
  --output-directory out \
  --markdown
```

This generates:

- `out/index.json`
- `out/index.md`

When `--output-directory` is omitted, outputs are written under `inputDirectory`.

## Batch Mode

Process each direct child directory under a parent directory:

```bash
java -jar miku-indexgen-<version>.jar \
  --input-parent-directory docs-parent \
  --output-directory out \
  --markdown
```

In this mode, each direct child directory is processed independently.
When `--output-directory` is specified, outputs are written under child-specific paths such as `out/<child>/index.json`.

## CLI Options

| Option | Description |
| --- | --- |
| `--input-directory <dir>` | Directory to scan. |
| `--input-parent-directory <dir>` | Process each direct child directory under the specified parent directory. |
| `--output-directory <dir>` | Directory to write `index.json` and optional `index.md`. When omitted, outputs are written under the input directory. |
| `--title <text>` | Add a root-level title to generated JSON. |
| `--markdown` | Also generate `index.md`. |
| `--no-generator` | Omit root-level `generator` metadata from generated JSON. |
| `--json-summary-path <paths>` | Comma-separated JSON Pointer list used to extract JSON summaries, for example `/title,/name`. |
| `--no-recursive` | Disable recursive scanning inside each selected base directory. |
| `--no-overwrite` | Skip writing when the output file already exists. |
| `--include-ext <exts>` | Comma-separated list of file extensions to include, for example `md,json`. |
| `--input-encoding <encoding>` | Input text encoding. Supported values are `utf8` and `shift_jis`. |
| `--output-encoding <encoding>` | Output text encoding. Supported values are `utf8` and `shift_jis`. |
| `--verbose` | Emit progress diagnostics to `stderr`. |

Usage rule:

- specify either `--input-directory` or `--input-parent-directory`

## Maven Plugin

The Maven plugin is maintained in the separated `miku-indexgen-java-maven` repository.

- Maven plugin repository: <https://github.com/igapyon/miku-indexgen-java-maven>
- Runtime artifact used by the plugin: `jp.igapyon:miku-indexgen`

This repository owns the Java runtime, CLI, core API, runtime tests, and runtime release assets.

## More Information

- Development notes: `docs/development.md`
- Java application design: `docs/miku-soft-20-javaapp-design-v20260425.md`
- Straight conversion policy: `docs/miku-soft-30-straight-conversion-v20260425.md`
- Upstream class mapping: `docs/upstream-class-mapping.md`
- Upstream test mapping: `docs/upstream-test-mapping.md`
- Migration status: `docs/remaining-migration-items.md`
