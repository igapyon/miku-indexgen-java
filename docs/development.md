# Development

This document is for repository maintainers and contributors.
For normal tool usage, see `README.md`.

## Scope

This document covers:

- repository structure
- local development commands
- focused regression commands
- local temporary workspace rules
- separated Maven plugin repository notes
- related project documents

This document does not try to be the user-facing usage guide.

## Repository Structure

This repository is a single-module runtime Maven project.

- repository root
  - runtime jar and CLI implementation
- `docs/`
  - migration, mapping, and development documents
- `workplace/`
  - local upstream checkout and temporary local work area

The Maven plugin is maintained in the separated `miku-indexgen-java-maven` repository.

## Core Design

Index generation is centered on `Indexgen.createIndexes(IndexgenOptions)`.

- CLI parses arguments and converts them into `IndexgenOptions`
- directory traversal and child-directory batch behavior are shared in runtime-side code
- the separated Maven plugin should remain a thin adapter over this runtime API

This repository intentionally keeps product behavior in the runtime module.

## Primary Commands

Use these commands for routine local verification:

```bash
mvn test
mvn package
```

## Focused Regression Commands

Run targeted tests when working on a specific area:

```bash
mvn test -Dtest=IndexgenTest
mvn test -Dtest=MarkdownTest
mvn test -Dtest=JsonSummaryTest
mvn test -Dtest=PathUtilsTest
mvn test -Dtest=EncodingTest
mvn test -Dtest=MikuIndexgenCliTest
```

## Local Temporary Work

Use `workplace/tmp` for manual smoke inputs and generated outputs.

Rules:

- keep `workplace/.gitkeep` only as tracked content
- do not commit local smoke inputs or generated outputs under `workplace/`
- use `workplace/miku-indexgen` as the temporary upstream checkout when needed

`workplace/` is for local reference and temporary verification, not for primary implementation files.

## Packaging Notes

`mvn package` currently produces these main artifacts:

- `target/miku-indexgen-1.6.0.jar`
- `target/miku-indexgen-1.6.0-sources.jar`
- `target/miku-indexgen-dist-1.6.0.zip`

The GitHub release workflow currently uploads the runtime jar and runtime source jar artifacts for end users.

## Maven Plugin Notes

The Maven plugin is maintained separately:

- <https://github.com/igapyon/miku-indexgen-java-maven>

Maven plugin tests, smoke commands, plugin examples, plugin release work, and plugin-facing parameter documentation belong to that repository.

## Child-Directory Batch Mode

This repository currently supports a Java-side `child-directory batch` execution mode.

Current contract:

- `inputParentDirectory` selects a parent directory
- the parent directory itself is not processed as an input base
- only direct child directories are selected
- direct child files are ignored
- hidden child directories are skipped
- `recursive` still means recursion inside each selected child base directory
- when `outputDirectory` is omitted, outputs are written under each child directory
- when `outputDirectory` is specified, outputs are written under child-specific paths such as `<outputDirectory>/<child>/index.json`
- child failures are aggregated, remaining children continue processing, and CLI execution exits non-zero when any child fails

This is a Java-side extension contract and should not be confused with the upstream-facing single-input contract discussion.

## Related Documents

Use these documents together, depending on the task:

- `README.md`
  - user-facing entry point
- `docs/miku-soft-reference.md`
  - shared miku-soft reference entry point
- `docs/input-files-spec.md`
  - input directory, file, metadata, and scan behavior
- `docs/index-json-spec.md`
  - generated `index.json` structure and maintenance rules
- `docs/miku-indexgen-frontmatter-spec.md`
  - Markdown front matter metadata policy
- `docs/upstream-class-mapping.md`
  - `upstream file -> Java class` mapping
- `docs/upstream-test-mapping.md`
  - `upstream test intent -> Java test` mapping
- `docs/upstream-followup-log.md`
  - known diffs, follow-up items, and verification history
- `docs/remaining-migration-items.md`
  - current migration state and latest verification notes
- `TODO.md`
  - open design and follow-up tasks
