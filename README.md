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

## Build

```bash
mvn test
mvn package
```

The distributable runtime artifact is a single fat jar:

- `miku-indexgen/target/miku-indexgen.jar`

`mvn package` also produces:

- `miku-indexgen/target/miku-indexgen-dist.zip`

## Core API Direction

The Java implementation keeps index generation behind `Indexgen.createIndexes(IndexgenOptions)`.
The CLI converts command-line arguments into `IndexgenOptions` and calls the same core API that a future Maven plugin goal should use.

## Maven Plugin

The Maven plugin is provided as a separate module:

- `miku-indexgen-maven-plugin`

Explicit execution with full coordinates:

```bash
mvn jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index
```

Example:

```bash
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index \
  -Dmiku-indexgen.targetDir=docs \
  -Dmiku-indexgen.markdown=true
```

The intended Maven plugin direction remains explicit execution first. The short form is:

```bash
mvn miku-indexgen:index
```

The short form requires Maven plugin prefix resolution for the `jp.igapyon` plugin group, such as a user or project Maven settings configuration.

Users who need automatic generation can opt in by binding that goal to a lifecycle phase such as `generate-resources`.

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
