# Development

## Primary Commands

```bash
mvn test
mvn package
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.targetDir=workplace/tmp/plugin-smoke -Dmiku-indexgen.markdown=true
```

## Local Temporary Work

Use `workplace/tmp` for manual smoke inputs and generated outputs.

`workplace/` contents other than `workplace/.gitkeep` are not tracked by Git, so this area is suitable for local verification artifacts.

## Maven Plugin Configuration

Maven plugin parameters can be passed through XML configuration in a consuming `pom.xml`.

```xml
<plugin>
  <groupId>jp.igapyon</groupId>
  <artifactId>miku-indexgen-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <targetDir>${project.basedir}/docs</targetDir>
    <markdown>true</markdown>
    <includeExtensions>
      <includeExtension>md</includeExtension>
      <includeExtension>json</includeExtension>
    </includeExtensions>
  </configuration>
</plugin>
```

Automatic lifecycle execution should be opt-in.

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
