# Development

## Primary Commands

```bash
mvn test
mvn package
mvn -N jp.igapyon:miku-indexgen-maven-plugin:0.1.0-SNAPSHOT:index -Dmiku-indexgen.targetDir=workplace/tmp/plugin-smoke -Dmiku-indexgen.markdown=true
```

## Local Temporary Work

Use `workplace/tmp` for manual smoke inputs and generated outputs.

`workplace/` contents other than `workplace/.gitkeep` are not tracked by Git, so this area is suitable for local verification artifacts.

## Focused Regression Commands

```bash
mvn test -Dtest=IndexgenTest
mvn test -Dtest=MikuIndexgenMojoTest
mvn test -Dtest=MarkdownTest
mvn test -Dtest=JsonSummaryTest
mvn test -Dtest=PathUtilsTest
mvn test -Dtest=EncodingTest
mvn test -Dtest=MikuIndexgenCliTest
```

## Upstream Reference

The initial upstream checkout is kept under:

```text
workplace/miku-indexgen
```

`workplace/` contents other than `workplace/.gitkeep` are not tracked by Git.
