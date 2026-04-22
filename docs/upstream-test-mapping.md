# Upstream Test Mapping

This document tracks `upstream test intent -> Java test` mapping for the miku-indexgen straight conversion.

```text
upstream test / intent:
  test/markdown.test.ts

java tests:
  jp.igapyon.mikuindexgen.markdown.MarkdownTest

fixtures:
  inline strings

focused regression:
  mvn test -Dtest=MarkdownTest
```

```text
upstream test / intent:
  test/json-summary.test.ts

java tests:
  jp.igapyon.mikuindexgen.jsonsummary.JsonSummaryTest

fixtures:
  inline JSON strings

focused regression:
  mvn test -Dtest=JsonSummaryTest
```

```text
upstream test / intent:
  test/path-utils.test.ts

java tests:
  jp.igapyon.mikuindexgen.pathutils.PathUtilsTest

fixtures:
  inline paths

focused regression:
  mvn test -Dtest=PathUtilsTest
```

```text
upstream test / intent:
  test/encoding.test.ts

java tests:
  jp.igapyon.mikuindexgen.encoding.EncodingTest

fixtures:
  temporary file from JUnit TempDir

focused regression:
  mvn test -Dtest=EncodingTest
```

```text
upstream test / intent:
  test/cli.test.ts

java tests:
  jp.igapyon.mikuindexgen.cli.MikuIndexgenCliTest

fixtures:
  inline argv arrays

focused regression:
  mvn test -Dtest=MikuIndexgenCliTest
```

```text
upstream test / intent:
  test/indexer-core.test.ts
  test/indexer-output.test.ts
  test/indexer-encoding.test.ts
  test/indexer-verbose.test.ts

java tests:
  pending

fixtures:
  pending

focused regression:
  pending
```
