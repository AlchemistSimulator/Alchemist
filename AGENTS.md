# Agent Instructions

Use repository-defined Gradle tasks from the repository root. Do not replace them with ad hoc shell commands or per-tool alternatives.

Run `./gradlew --parallel build` as the final validation step for any non-trivial change. If that fails, keep fixing the change and rerun until it passes or you hit an external blocker.

Format before the final validation run:
- If you changed Kotlin files, run `./gradlew --parallel ktlintFormat`.
- If you changed Scala files in `alchemist-incarnation-scafi`, run `./gradlew --parallel alchemist-incarnation-scafi:spotlessApply`.
- After formatting, rerun the relevant verification tasks and finish with `./gradlew --parallel build`.

Use narrower Gradle tasks only to iterate faster while working. Do not stop at `assemble`, `test`, or module-local checks when the change affects shared code, build logic, or release behavior.

Preserve the existing multi-module structure. Keep changes small, local, and consistent with the style already used in the touched module. Avoid mixing unrelated refactors into a focused fix.

Order the members of Java classes, interfaces, and enums as follows, extending the Checkstyle `DeclarationOrder` and `InnerTypeLast` checks:
1. Static fields.
2. Static initializers.
3. Instance fields.
4. Instance initializers.
5. Constructors.
6. Methods.
7. Static methods.
8. Nested types.
Within every group, sort members by visibility (public, then protected, then package-private, then private), fields sharing a visibility by mutability (`final` fields before non-final ones), and then members alphabetically by name, keeping overloads adjacent.

Order the members of Kotlin classes, interfaces, and objects as follows, extending the class layout of the Kotlin coding conventions:
1. Properties and `init` blocks, other than extension properties: first plain properties, without a custom getter or setter (private state included), then properties with custom accessors.
2. Secondary constructors.
3. Functions, including overrides, other than extension functions: first the non-private ones, then the private helpers.
4. Member extensions: extension properties before extension functions, and in each, non-private ones before private helpers.
5. Nested types used inside the class.
6. The companion object, whose members follow these same rules.
7. Nested types meant for use outside the class and not referenced inside it.
Within each group, keep members adjacent when they clearly work together, such as the members implementing the same supertype, or hooks next to the function that calls them.
Otherwise, sort them alphabetically by name, which also keeps overloads adjacent.
Apart from private helpers going last, do not sort by visibility, and do not reorder members only to satisfy visibility or alphabetical order.

Initialization order overrides every other ordering rule: fields, properties, and initializer blocks run in declaration order, so a member that reads another one during initialization must stay after it.
Reorder members only in the classes a change already touches, in a separate `style` commit unless asked otherwise.

Do not suppress warnings unless there is no real fix. Every suppression must be narrowly scoped and explained with a short justification next to the suppression site.

Treat generated files, caches, and build outputs as non-source artifacts. Do not commit them unless the task explicitly requires it.

Use Conventional Commits for any commit you create. Use the header format `type(scope): summary`; for breaking changes use `type(scope)!: summary` and add a `BREAKING CHANGE:` footer.

Prefer release-aware commit types:
- Use `feat` for user-visible features.
- Use `fix` for bug fixes.
- Use `docs`, `perf`, or `revert` when they match the actual change.
- Use `chore(api-deps)` for dependency updates expected to trigger a minor release.
- Use `chore(core-deps)` for dependency updates expected to trigger a patch release.
- Use `test`, `ci`, `build`, `style`, `refactor`, and other `chore(...)` scopes for non-release maintenance work.
- Use `chore(...)` or `ci(...)`, not `docs(...)`, for changes to agent instructions, skills, or automation policy.

If you touch the documentation site or Hugo pipeline, initialize submodules with `git submodule update --init --recursive` before running the relevant website tasks.
