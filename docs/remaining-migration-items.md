# Remaining Migration Items

## Current State

Straight conversion has started.

Implemented initial units:

- Maven / JUnit Jupiter / Java 1.8 project skeleton
- single fat jar packaging configuration
- distribution zip packaging configuration
- `workplace/` local upstream area
- `types.ts` model POJOs
- `markdown.ts` helper methods
- `json-summary.ts` summary extraction helpers
- `path-utils.ts` helpers
- `encoding.ts` encoding option helpers
- `cli.ts` argument parsing and help handling

## Pending

- `indexer.ts` Java conversion
- `logging.ts` Java conversion
- full CLI runtime index generation
- JSON output generation and deterministic ordering tests
- Markdown output file generation tests
- overwrite / recursive / include extension behavior tests
- verbose diagnostics tests
- package smoke test

## Focused Regression

- `mvn test`
- `mvn test -Dtest=MarkdownTest`
- `mvn test -Dtest=JsonSummaryTest`
- `mvn test -Dtest=PathUtilsTest`
- `mvn test -Dtest=EncodingTest`
- `mvn test -Dtest=MikuIndexgenCliTest`

## Latest Verification

- `2026-04-22`: `mvn test`
  - Tests run: 24
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `2026-04-22`: `mvn package`
  - Build success
  - Produced `target/miku-indexgen.jar`
  - Produced `target/miku-indexgen-dist.zip`
