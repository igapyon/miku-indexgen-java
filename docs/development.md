# Development

## Primary Commands

```bash
mvn test
mvn package
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index -Dmiku-indexgen.inputDirectory=workplace/tmp/plugin-smoke -Dmiku-indexgen.outputDirectory=workplace/tmp/plugin-out -Dmiku-indexgen.markdown=true
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index-child-directories -Dmiku-indexgen.inputParentDirectory=workplace/tmp/parent-smoke -Dmiku-indexgen.outputDirectory=workplace/tmp/parent-out -Dmiku-indexgen.markdown=true
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
    <inputDirectory>${project.basedir}/docs</inputDirectory>
    <outputDirectory>${project.build.directory}/generated-index</outputDirectory>
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

## Child-Directory-Batch Mode

This is the current Java-side extension contract for `child-directory-batch mode`.
It remains separate from the upstream-facing single-input contract.

### Purpose

When a parent directory `A` contains child directories such as `B1`, `B2`, and `B3`, this mode treats each direct child directory as an independent processing base directory.
This avoids repeating equivalent CLI or Maven plugin invocations for each child directory manually.

### Input Contract

- The user specifies a parent directory such as `A`.
- `A` itself is not processed as a base directory.
- Only direct child directories of `A` are selected as base directories.
- Direct child files under `A` are not selected as processing targets.
- Hidden directories are skipped during child directory discovery.

### Per-Child Behavior

- After a child directory such as `B1` is selected, processing returns to the normal per-directory behavior used today by `miku-indexgen-java`.
- `recursive` keeps its narrow meaning: whether processing recurses inside each selected child base directory.
- In other words, `child-directory-batch mode` decides how base directories are selected, and `recursive` decides how each selected base directory is scanned internally.

### Output Contract

- In existing `inputDirectory` mode, `outputDirectory` may be specified to place `index.json` and `index.md` outside the input tree.
- When `outputDirectory` is omitted in `inputDirectory` mode, outputs are written under `inputDirectory`.
- In `child-directory-batch mode`, when `outputDirectory` is omitted, outputs are written under each selected child directory.
- In `child-directory-batch mode`, when `outputDirectory` is specified, outputs are written under child-specific paths such as `<outputDirectory>/<child>/index.json`.

### Failure Contract

- Initial version behavior: stop on the first child directory failure.
- Partial-success aggregation is not part of the first version.

### CLI Expression

- Replace the old positional `targetDir` contract with explicit input-role naming.
- Add an explicit Java-only option for child discovery rather than overloading `recursive`.
- Current vocabulary:
- `--input-directory <dir>`: existing per-directory mode
- `--output-directory <dir>`: write `index.json` and optional `index.md` under the specified output directory
- `--input-parent-directory <dir>`: enable `child-directory-batch mode` and treat each direct child directory under the specified parent as an independent base directory
- `--no-recursive`: keep the existing meaning for per-directory scanning inside each selected child base directory
- `--verbose`: print progress diagnostics to stderr
- Usage examples:

```bash
miku-indexgen --input-directory docs --markdown --verbose
miku-indexgen --input-directory docs --output-directory out --markdown
miku-indexgen --input-parent-directory A --markdown --verbose
```

- Draft validation rules:
  - `--input-directory` and `--input-parent-directory` cannot be used together
  - `--input-parent-directory` does not change the meaning of `--no-recursive`; it only changes how base directories are selected
  - future options that assume a single processing base must be rejected when `--input-parent-directory` is active

### Maven Plugin Expression

- Existing per-directory goal: `index`
- Child-directory batch goal: `index-child-directories`
- Current vocabulary:
  - parameter: `inputParentDirectory`
  - reused parameter: `outputDirectory`
  - reused parameter: `recursive`
  - reused parameter: `verbose`
- Execution example:

```bash
mvn -N jp.igapyon:miku-indexgen-maven-plugin:1.0.0:index-child-directories \
  -Dmiku-indexgen.inputParentDirectory=A \
  -Dmiku-indexgen.markdown=true \
  -Dmiku-indexgen.verbose=true
```

- Plugin rules:
  - `index` and `index-child-directories` should remain separate execution contracts
  - `inputParentDirectory` belongs only to `index-child-directories`
  - child discovery, hidden-directory skipping, and stop-on-first-failure behavior should live in a shared runtime helper, not in the Mojo body

### Remaining Design Questions

- If failure aggregation is added later, define result reporting and exit-code behavior explicitly.

## Upstream Reference

The initial upstream checkout is kept under:

```text
workplace/miku-indexgen
```

`workplace/` contents other than `workplace/.gitkeep` are not tracked by Git.
