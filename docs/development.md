# Development

This document is for repository maintainers and contributors.
For normal tool usage, see `README.md`.

## Scope

This document covers:

- repository structure
- local development commands
- focused regression commands
- local temporary workspace rules
- Maven plugin development notes
- related project documents

This document does not try to be the user-facing usage guide.

## Repository Structure

This repository is a multi-module Maven reactor.

- repository root
  - aggregator project `miku-indexgen-java`
- `miku-indexgen/`
  - runtime jar and CLI implementation
- `miku-indexgen-maven-plugin/`
  - Maven plugin implementation
- `docs/`
  - migration, mapping, and development documents
- `workplace/`
  - local upstream checkout and temporary local work area

The runtime jar and the Maven plugin are separate deliverables, but both use the same core API.

## Core Design

Index generation is centered on `Indexgen.createIndexes(IndexgenOptions)`.

- CLI parses arguments and converts them into `IndexgenOptions`
- Maven plugin maps plugin parameters into the same `IndexgenOptions`
- directory traversal and child-directory batch behavior are shared in runtime-side code instead of being reimplemented in each adapter

This repository intentionally keeps CLI and Maven plugin layers thin.

## Primary Commands

Use these commands for routine local verification:

```bash
mvn test
mvn package
```

Useful Maven plugin smoke commands:

```bash
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.inputDirectory=workplace/tmp/plugin-smoke -Dmiku-indexgen.outputDirectory=workplace/tmp/plugin-out -Dmiku-indexgen.markdown=true
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index-child-directories -Dmiku-indexgen.inputParentDirectory=workplace/tmp/parent-smoke -Dmiku-indexgen.outputDirectory=workplace/tmp/parent-out -Dmiku-indexgen.markdown=true
```

## Focused Regression Commands

Run targeted tests when working on a specific area:

```bash
mvn test -Dtest=IndexgenTest
mvn test -Dtest=MikuIndexgenMojoTest
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

- `miku-indexgen/target/miku-indexgen-1.0.0.jar`
- `miku-indexgen/target/miku-indexgen-dist-1.0.0.zip`
- `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin-1.0.0.jar`

The GitHub release workflow currently uploads the runtime jar artifact for end users.

## Maven Plugin Notes

The Maven plugin is a first-class execution path, but lifecycle binding should remain opt-in.

Recommended approach:

- explicit execution first
- lifecycle binding only in consuming projects that want automatic generation

Full-coordinate execution works without plugin prefix resolution:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index
```

Short-form execution requires Maven plugin prefix resolution for the `jp.igapyon` plugin group:

```bash
mvn miku-indexgen:index
```

Minimal `pom.xml` example:

```xml
<plugin>
  <groupId>jp.igapyon</groupId>
  <artifactId>miku-indexgen-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <inputDirectory>${project.basedir}/docs</inputDirectory>
    <outputDirectory>${project.build.directory}/generated-index</outputDirectory>
    <markdown>true</markdown>
  </configuration>
</plugin>
```

Optional lifecycle binding example:

```xml
<executions>
  <execution>
    <id>generate-docs-index</id>
    <phase>generate-resources</phase>
    <goals>
      <goal>index</goal>
    </goals>
  </execution>
</executions>
```

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
- current behavior stops on the first child failure

This is a Java-side extension contract and should not be confused with the upstream-facing single-input contract discussion.

## Related Documents

Use these documents together, depending on the task:

- `README.md`
  - user-facing entry point
- `docs/miku-straight-conversion-guide.md`
  - common straight conversion principles for miku Java ports
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
