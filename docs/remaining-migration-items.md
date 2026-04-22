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
- `indexer.ts` core index generation
- `logging.ts` verbose log accumulation
- core-side `IndexgenOptions` / `IndexgenResult` shared by CLI and Maven plugin adapters
- multi-module Maven structure
- `miku-indexgen` runtime jar module
- `miku-indexgen-maven-plugin` Maven plugin module
- Maven plugin `index` goal

## Pending

- additional Maven plugin integration coverage as needed for future parameter expansion
- short prefix execution documentation for `mvn miku-indexgen:index`

## Focused Regression

- `mvn test`
- `mvn test -Dtest=IndexgenTest`
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
  - Produced `miku-indexgen/target/miku-indexgen-1.0.0.jar`
  - Produced `miku-indexgen/target/miku-indexgen-dist-1.0.0.zip`
- `2026-04-22`: `mvn test`
  - Tests run: 30
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `2026-04-22`: `mvn package`
  - Build success
  - Produced `miku-indexgen/target/miku-indexgen-1.0.0.jar`
  - Produced `miku-indexgen/target/miku-indexgen-dist-1.0.0.zip`
- `2026-04-22`: `java -jar miku-indexgen/target/miku-indexgen-1.0.0.jar /tmp/miku-indexgen-smoke --markdown`
  - Exit code: 0
  - Produced `index.json`
  - Produced `index.md`
- `2026-04-22`: `mvn test`
  - Reactor build success
  - Runtime tests run: 30
  - Maven plugin tests run: 2
- `2026-04-22`: `mvn package`
  - Reactor build success
  - Produced `miku-indexgen/target/miku-indexgen-1.0.0.jar`
  - Produced `miku-indexgen/target/miku-indexgen-dist-1.0.0.zip`
  - Produced `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin-1.0.0.jar`
- `2026-04-22`: `mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.targetDir=/tmp/miku-indexgen-plugin-smoke -Dmiku-indexgen.markdown=true`
  - Build success
  - Produced `index.json`
  - Produced `index.md`
- `2026-04-22`: `mvn miku-indexgen:index -N -Dmiku-indexgen.targetDir=/tmp/miku-indexgen-plugin-smoke -Dmiku-indexgen.markdown=true`
  - Failed because Maven plugin prefix resolution did not include the `jp.igapyon` plugin group
  - Full-coordinate execution remains the confirmed path
- `2026-04-22`: `mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.targetDir=workplace/tmp/plugin-smoke -Dmiku-indexgen.markdown=true`
  - Build success
  - Produced `workplace/tmp/plugin-smoke/index.json`
  - Produced `workplace/tmp/plugin-smoke/index.md`
- `2026-04-22`: Maven plugin XML configuration examples added to README and development docs
- `2026-04-22`: `docs/miku-straight-conversion-guide.md` updated to describe optional Maven plugin modules and the resulting multi-module layout
- `2026-04-22`: `mvn package`
  - Reactor build success
  - Produced `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin.jar`
- `2026-04-22`: plugin jar file name updated to include version `1.0.0`
  - Expected artifact path: `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin-1.0.0.jar`
- `2026-04-22`: `mvn package`
  - Reactor build success
  - Produced `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin-1.0.0.jar`
- `2026-04-22`: Maven coordinates updated to `1.0.0`
  - Parent: `jp.igapyon:miku-indexgen-java:1.0.0`
  - Runtime: `jp.igapyon:miku-indexgen:1.0.0`
  - Plugin: `jp.igapyon:miku-indexgen-maven-plugin:1.0.0`
- `2026-04-22`: `mvn install`
  - Reactor build success
  - Installed `1.0.0` artifacts to the local Maven repository
- `2026-04-22`: `mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.targetDir=workplace/tmp/plugin-smoke -Dmiku-indexgen.markdown=true`
  - Build success
  - Produced `workplace/tmp/plugin-smoke/index.json`
  - Produced `workplace/tmp/plugin-smoke/index.md`
