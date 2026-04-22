# Development

## Primary Commands

```bash
mvn test
mvn package
```

## Focused Regression Commands

```bash
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
