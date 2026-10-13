# Agent Instructions

Use repository-defined Gradle tasks from the repository root. Do not replace them with ad hoc shell commands or per-tool alternatives.

Run `./gradlew --parallel build` as the final validation step for any non-trivial change. If that fails, keep fixing the change and rerun until it passes or you hit an external blocker.

Format before the final validation run:
- If you changed Kotlin files, run `./gradlew --parallel ktlintFormat`.
- If you changed Scala files in `alchemist-incarnation-scafi`, run `./gradlew --parallel alchemist-incarnation-scafi:spotlessApply`.
- After formatting, rerun the relevant verification tasks and finish with `./gradlew --parallel build`.

Use narrower Gradle tasks only to iterate faster while working. Do not stop at `assemble`, `test`, or module-local checks when the change affects shared code, build logic, or release behavior.

Preserve the existing multi-module structure. Keep changes small, local, and consistent with the style already used in the touched module. Avoid mixing unrelated refactors into a focused fix.

Order the members of classes, interfaces, and objects as follows, in Java and Kotlin alike.
The order extends the Checkstyle `DeclarationOrder` and `InnerTypeLast` checks, and keeps Kotlin companion objects last as in the Kotlin coding conventions.
Within every group, sort members by visibility (public, then protected and Kotlin `internal`, then Java package-private, then private), and members sharing a visibility alphabetically by name, keeping overloads adjacent.
1. Java static fields.
2. Java static initializers.
3. Fields and Kotlin properties.
4. Initializer blocks and Kotlin `init` blocks.
5. Constructors (in Kotlin, secondary constructors).
6. Methods and Kotlin functions, including overrides and member extensions.
7. Java static methods.
8. Nested types, including Kotlin nested objects other than the companion.
9. The Kotlin companion object, whose members follow these same rules.

Initialization order overrides alphabetical order: fields, properties, and initializer blocks run in declaration order, so a member that reads another one during initialization must stay after it.
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
