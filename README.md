# miku-indexgen-java

Java port workspace for `miku-indexgen`.

## Purpose

This repository is a Java 1.8 based port of `miku-indexgen`.

The Java port does not try to redesign the upstream project into a Java-first architecture at the initial stage.
The primary goal is to preserve the upstream Node.js / TypeScript structure, naming, and intent closely enough that upstream changes remain traceable.

The first conversion scope is focused on:

- file index domain model
- Markdown summary and Markdown index helpers
- JSON summary extraction
- path and encoding helpers
- CLI argument and runtime entrypoint
- Maven plugin ready core API
- single fat jar packaging

## Upstream Policy

- Use `workplace/miku-indexgen` as the temporary upstream checkout during the initial conversion.
- Keep `workplace/` out of Git tracking except for `workplace/.gitkeep`.
- Keep Java implementation and Java-specific specs outside `workplace/`.

## Porting Policy

- Keep Java package names under `jp.igapyon.mikuindexgen`.
- Respect upstream file boundaries and responsibility splits as much as practical.
- Prefer names that are easy to map back to upstream `kebab-case` files and `camelCase` methods.
- Do not add new features during the initial straight conversion.

## Repository Structure

This repository is now a multi-module Maven reactor.

- the repository root is the aggregator project `miku-indexgen-java`
- the runtime jar implementation lives under `miku-indexgen/`
- the Maven plugin implementation lives under `miku-indexgen-maven-plugin/`

This means the main Java sources are intentionally one directory deeper than an initial single-module layout.
The deeper layout was introduced to keep the CLI runtime jar and the Maven plugin as separate deliverables while still sharing the same core API and version line.

## Build

```bash
mvn test
mvn package
```

The distributable runtime artifact is a single fat jar:

- `miku-indexgen/target/miku-indexgen-1.0.0.jar`

`mvn package` also produces:

- `miku-indexgen/target/miku-indexgen-dist-1.0.0.zip`

## Core API Direction

The Java implementation keeps index generation behind `Indexgen.createIndexes(IndexgenOptions)`.
The CLI converts command-line arguments into `IndexgenOptions` and calls the same core API that the Maven plugin goal also uses.

## Maven Plugin

The Maven plugin is provided as a separate module:

- `miku-indexgen-maven-plugin`

The plugin goal is:

- `index`

The goal generates `index.json` under `targetDir`.
When `markdown` is `true`, it also generates `index.md` under the same directory.

### Explicit Execution

Use full coordinates when running the plugin without relying on Maven plugin prefix resolution:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index
```

Example:

```bash
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index \
  -Dmiku-indexgen.targetDir=docs \
  -Dmiku-indexgen.markdown=true
```

This style is useful for one-shot execution and smoke verification.

### POM Configuration

Maven plugin parameters can be written in the consuming project's `pom.xml`.

Minimal example:

```xml
<plugin>
  <groupId>jp.igapyon</groupId>
  <artifactId>miku-indexgen-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <targetDir>${project.basedir}/docs</targetDir>
    <markdown>true</markdown>
  </configuration>
</plugin>
```

Fuller example:

```xml
<plugin>
  <groupId>jp.igapyon</groupId>
  <artifactId>miku-indexgen-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <targetDir>${project.basedir}/docs</targetDir>
    <outputFileName>index.json</outputFileName>
    <markdown>true</markdown>
    <recursive>true</recursive>
    <overwrite>true</overwrite>
    <verbose>false</verbose>
    <includeExtensions>
      <includeExtension>md</includeExtension>
      <includeExtension>json</includeExtension>
    </includeExtensions>
    <jsonSummaryPaths>
      <jsonSummaryPath>/title</jsonSummaryPath>
      <jsonSummaryPath>/metadata/title</jsonSummaryPath>
      <jsonSummaryPath>/description</jsonSummaryPath>
    </jsonSummaryPaths>
    <inputEncoding>utf8</inputEncoding>
    <outputEncoding>utf8</outputEncoding>
  </configuration>
</plugin>
```

### Lifecycle Binding

The intended Maven plugin direction remains explicit execution first.
Users who need automatic generation can opt in by binding the goal to a lifecycle phase such as `generate-resources`.

Lifecycle binding example:

```xml
<plugin>
  <groupId>jp.igapyon</groupId>
  <artifactId>miku-indexgen-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <targetDir>${project.basedir}/docs</targetDir>
    <markdown>true</markdown>
  </configuration>
  <executions>
    <execution>
      <id>generate-docs-index</id>
      <phase>generate-resources</phase>
      <goals>
        <goal>index</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

### Parameters

| Parameter | Default | Description |
| --- | --- | --- |
| `targetDir` | `${project.basedir}` | Directory to scan. |
| `outputFileName` | `index.json` | JSON output file name under `targetDir`. |
| `title` | unset | Optional root-level title in generated JSON. |
| `markdown` | `false` | Also generate `index.md`. |
| `includeGeneratorMetadata` | `true` | Include root-level `generator` metadata. |
| `jsonSummaryPaths` | unset | JSON Pointer list used to extract JSON summaries. |
| `recursive` | `true` | Recurse into nested subdirectories. |
| `overwrite` | `true` | Overwrite existing output files. |
| `verbose` | `false` | Emit verbose progress and timing logs. |
| `includeExtensions` | `md`, `json` | File extensions to include. |
| `inputEncoding` | `utf8` | Input text encoding. Supported values are `utf8` and `shift_jis`. |
| `outputEncoding` | `utf8` | Output text encoding. Supported values are `utf8` and `shift_jis`. |
| `skip` | `false` | Skip plugin execution. |

### Prefix Resolution

The short form is:

```bash
mvn miku-indexgen:index
```

The short form requires Maven plugin prefix resolution for the `jp.igapyon` plugin group, such as a user or project Maven settings configuration.

Full-coordinate execution does not require this prefix setup:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index
```

### Maven Plugin vs Antrun

Use the Maven plugin when the project should call `miku-indexgen` as a Maven goal.

Use `maven-antrun-plugin` when the project should execute the runtime jar directly.

Antrun example:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-antrun-plugin</artifactId>
  <version>3.1.0</version>
  <executions>
    <execution>
      <id>generate-docs-index</id>
      <phase>generate-resources</phase>
      <goals>
        <goal>run</goal>
      </goals>
      <configuration>
        <target>
          <java jar="${project.basedir}/tools/miku-indexgen-1.0.0.jar" fork="true" failonerror="true">
            <arg value="${project.basedir}/docs"/>
            <arg value="--markdown"/>
          </java>
        </target>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The plugin module package artifact is:

- `miku-indexgen-maven-plugin/target/miku-indexgen-maven-plugin-1.0.0.jar`

## Development Docs

Use this order when continuing migration work:

1. `docs/remaining-migration-items.md`
2. `docs/miku-straight-conversion-guide.md`
3. `docs/upstream-class-mapping.md`
4. `docs/upstream-test-mapping.md`
5. `docs/development.md`
6. `docs/upstream-followup-log.md`
