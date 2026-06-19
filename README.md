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

When outputs are written, the CLI reports one status line per output file:

- `add   :` newly created
- `update:` rewritten because content changed
- `none  :` unchanged content, so the file was left as-is

When `--output-directory` is omitted, outputs are written under `inputDirectory`.
Generated `index.json` includes generation metadata so the same index can be refreshed later.

Refresh an existing generated index:

```bash
java -jar miku-indexgen-<version>.jar \
  --refresh-index out/index.json
```

## Batch Mode

`--input-parent-directory` is a Java runtime extension for child-directory batch processing.
It is separate from the upstream Node.js runtime's single input directory mode.

Process each direct child directory under a parent directory:

```bash
java -jar miku-indexgen-<version>.jar \
  --input-parent-directory docs-parent \
  --output-directory out \
  --markdown
```

In this mode, each direct child directory is processed independently.
When `--output-directory` is specified, outputs are written under child-specific paths such as `out/<child>/index.json`.
If one child directory fails, the remaining child directories are still processed.
The command reports failed children to `stderr` and exits with a non-zero status when any child fails.

## CLI Options

| Option | Description |
| --- | --- |
| `--input-directory <dir>` | Directory to scan. |
| `--input-parent-directory <dir>` | Process each direct child directory under the specified parent directory. |
| `--refresh-index <index.json>` | Regenerate an existing index from its generation metadata. |
| `--output-directory <dir>` | Directory to write `index.json` and optional `index.md`. When omitted, outputs are written under the input directory. |
| `--title <text>` | Add a root-level title to generated JSON. |
| `--markdown` | Also generate `index.md`. |
| `--no-generator` | Omit root-level `generator` metadata from generated JSON. |
| `--json-summary-path <paths>` | Comma-separated JSON Pointer list used to extract JSON summaries, for example `/title,/name`. |
| `--no-recursive` | Disable recursive scanning inside each selected base directory. |
| `--no-overwrite` | Skip writing when the output file already exists. |
| `--include-ext <exts>` | Comma-separated list of file extensions to include, for example `md,json`. |
| `--exclude-glob <pattern>` | Exclude files by input-relative POSIX glob after extension filtering. Repeatable. Supports `*`, `?`, and `**`. |
| `--input-encoding <encoding>` | Input text encoding. Supported values are `utf8` and `shift_jis`. |
| `--output-encoding <encoding>` | Output text encoding. Supported values are `utf8` and `shift_jis`. |
| `--verbose` | Emit progress diagnostics to `stderr`. |

Usage rule:

- specify exactly one input mode: `--input-directory`, `--input-parent-directory`, or `--refresh-index`
- `--no-recursive` controls scanning inside each selected input base; it does not change how child directories are selected in batch mode
- child-directory batch mode aggregates child failures and exits non-zero when any child fails
- outputs may be written under the input directory by default; the current run's `index.json` and optional `index.md` are excluded from `files[]`
- generated `files[]` entries are ordered by POSIX-style relative paths using UTF-16 code unit order
- `--exclude-glob` is evaluated after extension filtering and stored in `generation.excludeGlobs`
- supported Markdown front matter fields are `title`, `description`, `topics`, `category`, `status`, `audience`, `created`, `updated`, and `sources`
- `title`, `description`, and `topics` are primary scan-time file selection signals
- `category`, `status`, and `audience` help route which files to read next
- `sources`, `created`, and `updated` help judge provenance and freshness
- long Markdown front matter `description` values are shortened to 256 UTF-16 code units, including the trailing `...`

## Excluding Files

Use repeatable `--exclude-glob <pattern>` options to remove obvious noise after
the normal input range is selected by the input mode, recursion, and
`--include-ext`.

Patterns are evaluated against paths relative to each selected input directory.
Path separators are normalized to `/`. Matching is case-sensitive. The
supported glob syntax is intentionally small:

- `*` matches zero or more characters within one path segment
- `?` matches one character within one path segment
- `**` matches zero or more path segments

Example:

```bash
java -jar miku-indexgen-<version>.jar \
  --input-directory references/raw/mikuku-articles \
  --output-directory references/index/articles \
  --include-ext md \
  --exclude-glob "**/images-*/*" \
  --exclude-glob "**/images/*" \
  --exclude-glob "**/note-image-recovery.md" \
  --exclude-glob "**/image-prompt.md" \
  --exclude-glob "**/section-text.md"
```

`--exclude-glob` values are stored in `generation.excludeGlobs`, so
`--refresh-index` regenerates with the same exclusion rules.

## Maven Plugin

The Maven plugin is maintained in the separated `miku-indexgen-java-maven` repository.

- Maven plugin repository: <https://github.com/igapyon/miku-indexgen-java-maven>
- Runtime artifact used by the plugin: `jp.igapyon:miku-indexgen`

This repository owns the Java runtime, CLI, core API, runtime tests, and runtime release assets.

## Repository Maintenance

When checking README or documentation typos, use the `igapyon-miku-soft-developer` skill in typo-check mode.

## More Information

- Development notes: `docs/development.md`
- miku-soft shared reference: `docs/miku-soft-reference.md`
- Input files specification: `docs/input-files-spec.md`
- Generated `index.json` specification: `docs/index-json-spec.md`
- Markdown front matter specification: `docs/miku-indexgen-frontmatter-spec.md`
- Upstream class mapping: `docs/upstream-class-mapping.md`
- Upstream test mapping: `docs/upstream-test-mapping.md`
- Migration status: `docs/remaining-migration-items.md`
