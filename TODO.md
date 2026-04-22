# TODO

## Maven Plugin

- Done: add a Maven plugin goal as a first-class execution path.
- Primary usage should be explicit execution:

```bash
mvn miku-indexgen:index
```

- Do not bind the goal to a lifecycle phase by default.
- Users who need automatic generation can opt in by binding the goal to a phase such as `generate-resources`.
- Done: keep CLI and Maven plugin as thin wrappers over the same core API.
- Done: add core-side `IndexgenOptions` / `IndexgenResult` before implementing the plugin so CLI arguments and Maven plugin parameters can map to the same execution contract.
- Note: full-coordinate execution works without plugin prefix setup: `mvn jp.igapyon:miku-indexgen-maven-plugin:0.1.0-SNAPSHOT:index`.
- Note: short execution `mvn miku-indexgen:index` requires Maven plugin prefix resolution for the `jp.igapyon` plugin group.
- Use `workplace/tmp` for future manual smoke inputs and generated outputs where practical.
- If the Maven plugin execution path works well for this repository, consider updating `docs/miku-straight-conversion-guide.md` so future miku Java ports can evaluate Maven plugin goals as an optional first-class runtime path.
