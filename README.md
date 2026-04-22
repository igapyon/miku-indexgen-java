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

- `target/miku-indexgen.jar`

`mvn package` also produces:

- `target/miku-indexgen-dist.zip`

## Development Docs

Use this order when continuing migration work:

1. `docs/remaining-migration-items.md`
2. `docs/miku-straight-conversion-guide.md`
3. `docs/upstream-class-mapping.md`
4. `docs/upstream-test-mapping.md`
5. `docs/development.md`
6. `docs/upstream-followup-log.md`
