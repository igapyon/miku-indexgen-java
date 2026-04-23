# TODO

## Maven Plugin

- Done: add a Maven plugin goal as a first-class execution path.
- Primary usage should be explicit execution:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index
```

- Do not bind the goal to a lifecycle phase by default.
- Users who need automatic generation can opt in by binding the goal to a phase such as `generate-resources`.
- Done: keep CLI and Maven plugin as thin wrappers over the same core API.
- Done: add core-side `IndexgenOptions` / `IndexgenResult` before implementing the plugin so CLI arguments and Maven plugin parameters can map to the same execution contract.
- Note: full-coordinate execution works without plugin prefix setup: `mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index`.
- Note: short execution `mvn miku-indexgen:index` requires Maven plugin prefix resolution for the `jp.igapyon` plugin group.
- Use `workplace/tmp` for future manual smoke inputs and generated outputs where practical.
- Done: update `docs/miku-straight-conversion-guide.md` so future miku Java ports can treat Maven plugin goals as a high-priority first-class execution path for CLI / batch style tools.

## Directory / Batch Extension Alignment

- The updated `docs/miku-straight-conversion-guide.md` now treats Java-side `directory / batch` handling as an explicit extension contract, not as the upstream single-input core contract.
- The current implementation is still directory-first in both CLI and Maven plugin, so the next work is not just feature addition but contract separation and naming cleanup.

### Contract Split

- TODO: define the upstream-facing single-input contract and the Java-only `directory / batch` contract separately in README, CLI help, and regression docs.
- TODO: decide whether `miku-indexgen` keeps the current directory scan behavior as a Java extension or whether a new single-file entrypoint becomes the primary straight-conversion contract.
- Done: remove Java-side `--output <fileName>` / `outputFileName` style naming overrides and simplify the contract to fixed output names such as `index.json` and `index.md`.
- TODO: document mutually exclusive option sets up front, especially combinations equivalent to `inputDirectory` with any future archive-style single-output options.
- Done: rename the existing per-directory contract from `targetDir` to `inputDirectory` so the argument name reflects its role before more directory modes are added.

### Core / Runtime Structure

- Done: move directory traversal / repeated execution for child-directory batch handling into shared runtime-side code that is reused by CLI and Maven plugin.
- Done: keep shared directory / batch behavior out of the Maven plugin body and reuse the same runtime-side implementation from both execution paths.
- TODO: decide and document whether generated outputs may be written into the input directory by default, and only allow that when re-scanning of generated files is prevented by contract.

### CLI Tasks

- TODO: introduce explicit Java-only naming for directory / batch execution so it is distinguishable from normal single-input commands or options.
- TODO: add entry validation for conflicting option combinations in directory mode and keep those failures as usage errors, not warnings.
- TODO: define `recursive` narrowly as "whether to recurse inside each selected base directory" and keep it separate from the question of how base directories are selected.
- Done: implement Java-side `child-directory-batch mode` for the case where a parent directory `A` is given and each direct child directory `B1`, `B2`, `B3` becomes an independent processing base directory.
- Done: in `child-directory-batch mode`, keep `A` itself out of the processing targets and treat only direct child directories as targets.
- Done: in `child-directory-batch mode`, skip hidden directories when discovering child base directories.
- Done: in `child-directory-batch mode`, once a child base directory is selected, apply the normal per-directory behavior from that child onward, including the usual `recursive` handling inside that child.
- TODO: add or refine stderr-only verbose / progress diagnostics for long-running or multi-file CLI processing.
- TODO: extend CLI regression tests so help text, usage text, stdout / stderr split, exit code, and directory-mode validation stay aligned.

### Maven Plugin Tasks

- Done: keep Maven plugin goals as thin adapters over the same runtime helper used by the CLI for directory / batch execution.
- TODO: if directory / batch parameters are added or renamed, keep Maven vocabulary aligned with CLI vocabulary such as `inputDirectory`, `outputDirectory`, and `recursive`.
- TODO: reject plugin parameter combinations that do not make sense together, and describe the same restrictions in README.
- TODO: decide whether the current `index` goal remains the directory-oriented Java extension goal or whether a thinner single-input goal should be introduced separately.
- TODO: add regression tests that lock shared directory traversal, relative path handling, output naming, and conflict validation at the runtime-helper level, with plugin tests kept focused on parameter mapping and logging.

### Open Design Decisions

- Done: support per-child output behavior when `outputDirectory` is omitted and child-specific shared output paths when `outputDirectory` is specified.
- Done: avoid output-name collisions in shared batch output by writing each child directory under its own child-specific output path.
- Done: for the initial `child-directory-batch mode` draft, stop on the first child directory failure.
- TODO: continue design discussion for any later aggregate-result mode in `child-directory-batch mode`, including exit-code and reporting semantics.

### Documentation Sync

- TODO: update README so it explicitly distinguishes the straight-conversion core contract from Java-only directory / batch extensions.
- TODO: update `docs/development.md` focused regression commands once directory / batch tests are split into core-helper, CLI, and Maven plugin layers.
- TODO: keep future wording synchronized across README, CLI help, Maven plugin parameter docs, and regression notes so the same contract is described once and reused consistently.

### Upstream Follow-Up

- TODO: record for upstream Node.js / TypeScript that `targetDir` is also too ambiguous there once multiple input-selection modes are considered, and suggest role-based naming such as `inputDirectory` where feasible.
- TODO: record for upstream Node.js / TypeScript that configurable output file naming such as `--output <fileName>` may be unnecessary if the tool contract is intentionally fixed to `index.json` / `index.md`.
