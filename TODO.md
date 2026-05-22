# TODO

## Maven Plugin

- Done: add a Maven plugin goal as a first-class execution path.
- Done: separate Maven plugin ownership to `miku-indexgen-java-maven`.
- Runtime repository responsibility is now the Java runtime, CLI, core API, runtime tests, and runtime release assets.
- Maven plugin repository responsibility is plugin goals, parameters, Maven logging, plugin tests, examples, smoke scripts, and plugin release work.
- Primary usage should be explicit execution:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:<version>:index
```

- Do not bind the goal to a lifecycle phase by default.
- Users who need automatic generation can opt in by binding the goal to a phase such as `generate-resources`.
- Done: keep CLI and Maven plugin as thin wrappers over the same core API.
- Done: add core-side `IndexgenOptions` / `IndexgenResult` before implementing the plugin so CLI arguments and Maven plugin parameters can map to the same execution contract.
- Note: full-coordinate execution works without plugin prefix setup: `mvn jp.igapyon:miku-indexgen-maven-plugin:<version>:index`.
- Note: short execution `mvn miku-indexgen:index` requires Maven plugin prefix resolution for the `jp.igapyon` plugin group.
- Use `workplace/tmp` for future manual smoke inputs and generated outputs where practical.
- Done: update `docs/miku-straight-conversion-guide.md` so future miku Java ports can treat Maven plugin goals as a high-priority first-class execution path for CLI / batch style tools.

## Directory / Batch Extension Alignment

- The updated `docs/miku-straight-conversion-guide.md` now treats Java-side `directory / batch` handling as an explicit extension contract, not as the upstream single-input core contract.
- The current implementation is still directory-first in both CLI and Maven plugin, so the next work is not just feature addition but contract separation and naming cleanup.

### Contract Split

- Done: define the upstream-facing single-input contract and the Java-only `directory / batch` contract separately in README, CLI help, and regression docs.
- Done: keep directory scan behavior as the primary runtime contract; do not introduce a single-file entrypoint in this Java runtime.
- Done: remove Java-side `--output <fileName>` / `outputFileName` style naming overrides and simplify the contract to fixed output names such as `index.json` and `index.md`.
- Done: document mutually exclusive input modes up front: `inputDirectory`, `inputParentDirectory`, and `refreshIndex`.
- Done: rename the existing per-directory contract from `targetDir` to `inputDirectory` so the argument name reflects its role before more directory modes are added.

### Core / Runtime Structure

- Done: move directory traversal / repeated execution for child-directory batch handling into shared runtime-side code that is reused by CLI and Maven plugin.
- Done: keep shared directory / batch behavior out of the Maven plugin body and reuse the same runtime-side implementation from both execution paths.
- Done: document that generated outputs may be written into the input directory by default, and keep the current run's generated files excluded from `files[]`.

### CLI Tasks

- Done: use explicit Java-only `inputParentDirectory` / `--input-parent-directory` naming for child-directory batch execution.
- Done: add entry validation for conflicting input mode combinations and keep those failures as usage errors.
- Done: define `recursive` narrowly as "whether to recurse inside each selected base directory" and keep it separate from how base directories are selected.
- Done: implement Java-side `child-directory-batch mode` for the case where a parent directory `A` is given and each direct child directory `B1`, `B2`, `B3` becomes an independent processing base directory.
- Done: in `child-directory-batch mode`, keep `A` itself out of the processing targets and treat only direct child directories as targets.
- Done: in `child-directory-batch mode`, skip hidden directories when discovering child base directories.
- Done: in `child-directory-batch mode`, once a child base directory is selected, apply the normal per-directory behavior from that child onward, including the usual `recursive` handling inside that child.
- Done: keep verbose / progress diagnostics on stderr for CLI execution and cover stdout / stderr split in CLI regression tests.
- Done: extend CLI regression tests so help text, usage text, stdout / stderr split, exit code, and directory-mode validation stay aligned.

### Maven Plugin Tasks

- Done: keep Maven plugin goals as thin adapters over the same runtime helper used by the CLI for directory / batch execution.
- Done: change Maven plugin verbose / progress logging from buffered `IndexgenResult.logs` output to per-event logging through the Mojo logger, while keeping the shared core API usable from CLI and tests.
- Done: move Maven plugin implementation and plugin-owned follow-ups to the separated `miku-indexgen-java-maven` repository.
- Done: keep runtime API behavior source-compatible for the separated Maven plugin adapter; `1.3.0` adds optional generation metadata and refresh support without removing existing runtime fields.
- Done: add runtime-side regression tests that lock shared directory traversal, relative path handling, output naming, and conflict validation.

### Open Design Decisions

- Done: support per-child output behavior when `outputDirectory` is omitted and child-specific shared output paths when `outputDirectory` is specified.
- Done: avoid output-name collisions in shared batch output by writing each child directory under its own child-specific output path.
- Done: for the initial `child-directory-batch mode` draft, stop on the first child directory failure.
- Done: implement aggregate-result behavior for `child-directory-batch mode`; remaining child directories continue after a child failure, failed children are reported, and CLI exits non-zero when any child fails.

### Documentation Sync

- Done: update README so it explicitly distinguishes the straight-conversion core contract from Java-only directory / batch extensions.
- Done: update `docs/development.md` so Maven plugin checks are treated as separated-repository concerns.
- Done: synchronized runtime README, CLI help, separated Maven plugin parameter docs, and regression notes for runtime `1.3.0`.

### Upstream Follow-Up

- Done: upstream Node.js / TypeScript already uses `inputDirectory` in the current checked `origin/devel` source.
- Done: configurable output file naming is out of the current upstream and Java `1.3.0` contract; both use fixed `index.json` and optional `index.md`.
- Done: upstream Node.js / TypeScript README and docs are now separated into user-facing README and product-specific docs in the checked `origin/devel` source.

## 1.3.0 Follow-up

The Java runtime has followed upstream `miku-indexgen` `1.3.0` behavior for
generation metadata, `--refresh-index`, documented Markdown front matter
metadata fields, product-specific docs, and expanded CLI help.

Remaining follow-up items:

- Done: verified the separated `miku-indexgen-java-maven` repository against runtime `1.3.0`; no active `1.2.1` references remain outside historical context, and plugin tests / smoke passed.
- Done: keep `--refresh-index` as CLI/runtime behavior for this repository; any Maven plugin refresh goal or parameter is a separated `miku-indexgen-java-maven` design decision.
- Done: update stale references in this runtime repository; remaining `1.2.1` mentions are historical migration log entries.
- Done: repository references to copied `docs/miku-soft-*` documents were removed; IDE tabs may remain stale locally but should point to `docs/miku-soft-reference.md` going forward.
- Done: verified copied product-specific docs against upstream `miku-indexgen` `origin/devel` `07064d0`; only Java companion repository context wording differs.
- Done: no product-specific docs resynchronization is needed for the current upstream `origin/devel` `07064d0`; when upstream docs change in a later follow-up, compare `docs/input-files-spec.md`, `docs/index-json-spec.md`, and `docs/miku-indexgen-frontmatter-spec.md` before copying.

## Refactoring Candidates

These are behavior-preserving cleanup candidates found during the `1.3.0`
upstream follow-up. Keep them small and run `mvn test` after each slice.

### Low-Risk Cleanup

- Done: remove the unused `Markdown.parseInlineTopics` helper now that front matter arrays are handled through the newer string-array parser.
- Done: keep focused tests around unsupported front matter value shapes before moving parser code, so cleanup does not accidentally broaden the supported contract.

### Responsibility Split

- Done: extract Markdown front matter parsing from `Markdown` into `MarkdownFrontMatterParser`.
  - Keep `Markdown` responsible for summary extraction, Markdown table escaping, and Markdown index output.
  - Keep the parser contract aligned with `docs/miku-indexgen-frontmatter-spec.md`.
- Done: extract JSON output formatting from `Indexgen` into `IndexJsonFormatter`.
  - Move `buildFilesJson`, `buildGenerationJson`, `buildSourcesJson`, string-array formatting, and JSON quoting together.
  - Preserve the one-file-entry-per-line `files[]` formatting contract.
- Done: extract long CLI help text from `MikuIndexgenCli` into `HelpText`.
  - Keep parser behavior and display text testable without making CLI argument parsing harder to read.

### Package Boundary Cleanup

- Done: move the JSON parser out of `jsonsummary` into `jp.igapyon.mikuindexgen.json.JsonParser` for shared runtime JSON parsing.
- Done: keep product behavior in runtime classes and keep shared miku-soft guidance referenced through `docs/miku-soft-reference.md`, not copied back into the repository.
