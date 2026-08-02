# Reactive Engine Refactor Plan

Last updated: 2026-08-02

Working branch: `marmellata`

This is the living execution plan for the graphless reactive-engine refactor. Update it whenever work starts or
finishes, an architectural decision changes, a new blocker is discovered, or validation produces new evidence.
Do not mark an item complete until its implementation and proportional verification are complete.

## Fixed decisions

- Continue on `marmellata`; do not restart the refactor on a new branch.
- Merge `origin/master` into `marmellata`; do not replace the existing work with a replay from scratch.
- Remove the dependency graph and all dependency-descriptor APIs used only to maintain it.
- Remove `BatchEngine` and converge on one execution and scheduling model.
- Make `TimeDistribution` responsible only for generating correctly distributed time samples.
- Make reactions own `tau`, execution advancement, invalidation, and any transformation or replacement of a
  previously sampled time.
- Remove propensity contribution from conditions. Conditions describe reactive validity, not scheduling policy.
- Put chemical propensity computation in a specialized token-driven reaction abstraction.
- Renounce Java `Serializable` across the model and engine APIs.
- When a Java source file requires significant behavioral, structural, or API modification, port that file to
  Kotlin as part of the same change instead of extending the Java implementation.
- Eliminate the `org.danilopianini:javalib-java7:0.6.1` dependency by the end of the implementation. Replace
  `ListSet` with suitable Kotlin collection types or immutable collections while preserving required ordering and
  uniqueness semantics.
- Keep changes small and focused, and maintain this document as part of each phase.

The Java-to-Kotlin rule applies during every phase, not only during final API modernization. Small mechanical
edits needed for merge conflict resolution or temporary compilation recovery do not by themselves require a
port. A port must replace the Java declaration atomically, preserve intentional Java and Scala interoperability,
and must not leave duplicate fully qualified declarations behind.

## Target architecture

```text
observable model state
    |                         +-------------------------+
    +--> reactive validity -->| Reaction                |
    |                         | - owns tau              |--> observable tau --> Engine --> Scheduler
    +--> reaction token ----->| - owns invalidation     |
                              | - owns time adjustment  |
TimeDistribution ------------>| - requests new samples |
(generates time samples only) +-------------------------+
```

The engine must not infer model dependencies. It schedules reactions and reindexes them when their owned `tau`
changes. Model observables invalidate the reactions that directly consume them.

### Architectural invariants

1. A `TimeDistribution` does not know about `Actionable`, `Reaction`, `Environment`, conditions, propensity,
   scheduler state, or observable `tau`.
2. Drawing a new sample is observably different from adjusting an already sampled occurrence. A reaction decides
   which operation is semantically correct.
3. A reaction owns its absolute next-occurrence time and is the only component allowed to change it.
4. The engine observes reaction times but never computes them.
5. Conditions expose validity only. A condition cannot implicitly own chemical propensity policy.
6. A chemical reaction observes a propensity token containing every model value needed by its propensity law.
   Changes that alter propensity without changing Boolean validity must still invalidate the reaction.
7. Each logical model mutation transaction recomputes and reindexes every affected reaction at most once.
8. Removed or disposed reactions cannot emit scheduler updates.
9. All scheduling calculations use the triggering event's simulation time, never a stale batch/global time.

### Soundness constraints to settle before the core API is finalized

- Prefer distributions that sample non-negative delays; reactions convert delays to absolute `tau`. If an absolute
  time generator is required, document why it cannot use the delay contract.
- Define the chemical propensity token precisely. It must be a typed, reactive snapshot or observable source, not
  an opaque replacement for the old dependency metadata.
- Define adjustment semantics per reaction family. Scaling an exponential residual time is well-defined, but an
  arbitrary sampled distribution cannot necessarily be conditioned or rescaled correctly. Unsupported
  reaction/distribution combinations must fail clearly rather than silently redraw with different semantics.
- Decide whether invalidation preserves the existing random sample, transforms it, or explicitly redraws it. Test
  random-number consumption as part of the contract.

## Current evidence

- [x] Inspected the history from `2e50026544ac70ca54ae59e848cc315fe666bbce` through current `HEAD`.
- [x] Compared the reactive branch with the dependency-graph implementation on `origin/master`.
- [x] Confirmed that the worktree was clean before creating this plan.
- [x] Reproduced the current full-build failure.
- [x] Confirmed in an isolated snapshot that removing only the duplicate Kotlin `TimeDistribution` allows
  `alchemist-api` Kotlin and Java compilation to pass.
- [x] Identified the next masked compiler frontier in the incomplete `Environment`/`Neighborhood` collection
  migration.
- [ ] Merge the current `origin/master` and refresh this evidence after conflict resolution.

Known failures before the master merge:

- Java and Kotlin declare incompatible `it.unibo.alchemist.model.TimeDistribution` interfaces.
- `AbstractEnvironment` mixes immutable `List` declarations with mutation and stale `ListSet` APIs.
- `SimpleNeighborhood` contains an unfinished `ImmutableL` type and implements methods removed from the new API.
- `Neighborhood` consumers still require collection, iteration, membership, and copy-update semantics.
- Quality checks report formatting in `ConnectToAccessPoint`, missing documentation in `PhysicsUpdate`, and an
  empty initializer in `SendToNeighbor`.

## Phase 1: merge master on the current branch

- [x] Refresh `origin/master` immediately before the merge.
- [x] Record the pre-merge `HEAD`, current build failures, and worktree status.
- [ ] Merge `origin/master` into `marmellata` with a normal merge commit.
- [x] Resolve conflicts by preserving the reactive direction while adopting current master fixes and APIs.
- [x] Do not regenerate or commit caches, documentation caches, build outputs, or unrelated lock-file changes.
- [ ] Re-run focused compilation to establish the post-merge failure frontier.
- [ ] Update the current-evidence section with the merge commit and new failures.

## Phase 2: restore a compiling baseline

This phase restores coherence without prematurely finalizing the new scheduling API.

- [ ] Keep exactly one `TimeDistribution` declaration. Retain the currently implemented Java reactive contract
  temporarily if that minimizes churn before Phase 4.
- [ ] Complete or locally roll back the unfinished `Environment`/`Neighborhood` collection migration.
- [ ] Replace encountered `ListSet` uses with Kotlin collections or immutable collections; do not introduce new
  uses of `javalib-java7` while restoring compilation.
- [ ] Preserve ordered uniqueness for nodes, global reactions, and neighborhood members.
- [ ] Restore collection/iteration behavior required by Java, Kotlin, and Scala consumers.
- [ ] Fix the known formatting and static-analysis failures without suppressions.
- [ ] Compile `alchemist-api`, `alchemist-engine`, and `alchemist-implementationbase` before widening verification.
- [ ] Record newly exposed downstream compilation failures here.

## Phase 3: remove `BatchEngine`

- [ ] Delete `BatchEngine` and its output replay strategy.
- [ ] Audit `BatchedScheduler`, fixed-batch and epsilon-batch queues, constructors, loaders, configuration strings,
  tests, examples, and documentation.
- [ ] Remove batch-only scheduler types that no longer have a supported consumer.
- [ ] Migrate any generally useful scheduler behavior to the single `Engine` path.
- [ ] Remove batch-specific synchronization and stale-time accommodations.
- [ ] Add a compatibility error for removed batch-engine configuration where silent fallback would be dangerous.
- [ ] Verify that the remaining engine has one authoritative event-time and scheduler-update path.

## Phase 4: reduce `TimeDistribution` to sampling

- [ ] Define the minimal sampling contract, preferably a non-negative delay generator.
- [ ] Remove `update`, `reactToUpdate`, `Actionable`, `Environment`, propensity, rate-conditioning, and observable
  next-occurrence responsibilities from `TimeDistribution`.
- [ ] Decide whether configured base rate remains a distribution parameter or becomes entirely reaction-owned.
- [ ] Migrate deterministic, exponential, trigger, Weibull, network-arrival, molecule-controlled, and incarnation-
  specific implementations.
- [ ] Make invalid samples, negative delays, NaN, and infinity behavior explicit.
- [ ] Add isolated deterministic and statistical tests for every distribution family.

## Phase 5: make reactions own scheduling

- [ ] Give each reaction sole ownership of observable `tau`.
- [ ] Define initialization, firing, invalidation, cloning, and removal transitions.
- [ ] Move all decisions about sampling, preserving, transforming, or replacing scheduled times into reactions.
- [ ] Keep scheduler notification as a consequence of a reaction-owned `tau` change.
- [ ] Ensure initialization computes `tau` before scheduler insertion without causing duplicate reindexing.
- [ ] Make disposal idempotent and prevent post-removal emissions.
- [ ] Migrate global reactions and specialized reactions without duplicating the base lifecycle.

## Phase 6: simplify conditions and introduce chemical propensity tokens

- [ ] Remove propensity contribution from `Condition` and `AbstractCondition`.
- [ ] Remove the separate observable dependency set if direct reactive validity/token sources make it redundant.
- [ ] Define the specialized chemical reaction type and its propensity-token type.
- [ ] Make the token expose every concentration, match, stoichiometric quantity, or neighborhood value required to
  recompute propensity.
- [ ] Make token changes reactive even when condition validity remains `true`.
- [ ] Move mass-action and other propensity laws into the chemical reaction hierarchy.
- [ ] Define which time generators are valid for chemically conditioned reactions.
- [ ] Migrate biochemistry, SAPERE, and other propensity-aware implementations explicitly.
- [ ] Reject missing, negative, NaN, or otherwise invalid propensity values at the reaction boundary.

## Phase 7: implement reactive invalidation transactions

- [ ] Introduce an engine-owned transaction/dirty-reaction set around each model mutation and reaction execution.
- [ ] Mark reactions dirty synchronously when their reactive inputs change.
- [ ] Recompute each dirty reaction once after the mutation finishes.
- [ ] Reindex each changed `tau` once after recomputation.
- [ ] Define behavior for invalidation originating from scheduled commands outside reaction execution.
- [ ] Guard observable callback iteration against subscription mutation and re-entrant emissions.
- [ ] Verify deterministic ordering when multiple reactions are invalidated together.

## Phase 8: remove Java serialization

- [ ] Inventory `Serializable`, `serialVersionUID`, `@Serial`, `@Transient`, `readObject`, `writeObject`, and cloning
  code coupled to serialization.
- [ ] Remove serialization inheritance from model, engine, reaction, condition, action, node, environment, molecule,
  and time-distribution APIs where present.
- [ ] Remove obsolete serialization fields, hooks, tests, and warning suppressions.
- [ ] Check exporters, UI tools, distributed execution, and loaders for accidental reliance on Java object streams.
- [ ] If persistence remains necessary, define explicit versioned DTOs outside the live reactive object graph.
- [ ] Document the breaking change.

## Phase 9: complete observable model migration

- [ ] Audit every condition and reaction for non-observable reads that affect validity, tokens, or scheduling.
- [ ] Cover node contents, molecule presence, neighborhoods, positions, node counts, ranges, layers, and global state.
- [ ] Audit biochemistry, Protelis, SAPERE, Scafi, cognitive agents, physics, maps, and global reactions.
- [ ] Remove topology-driven dependency maintenance only after equivalent observable invalidation is tested.
- [ ] Remove obsolete `Context` uses from scheduling; retain context only if it still has independent semantic value.
- [ ] Verify reaction and node addition/removal clean up every subscription.

## Phase 10: behavioral and regression coverage

- [ ] Port old dependency-graph expectations as observable behavior tests, not graph-structure tests.
- [ ] Test positive local invalidation and negative isolation across unrelated nodes.
- [ ] Test neighborhood addition/removal, movement, global changes, and dynamic reaction/node changes.
- [ ] Test propensity changes that do not change Boolean condition validity.
- [ ] Test zero-to-positive, positive-to-zero, and positive-to-positive chemical propensity transitions.
- [ ] Verify whether each transition preserves, transforms, or redraws the sampled time as specified.
- [ ] Assert random-number consumption explicitly for stochastic generators.
- [ ] Test one recomputation and scheduler reindex per logical transaction.
- [ ] Test observer cleanup and absence of post-removal scheduler updates.
- [ ] Add performance checks for large reaction populations and high-frequency invalidation.

## Phase 11: finish API modernization

- [ ] Migrate remaining Java APIs to Kotlin one type at a time; Java files modified significantly in earlier
  phases must already have been ported as part of those changes.
- [ ] Never leave duplicate fully qualified Java and Kotlin declarations during a port.
- [ ] Finalize the `ListSet` replacements and ordered immutable collection choices without weakening uniqueness
  guarantees or exposing internally mutable collections.
- [ ] Remove the `org.danilopianini:javalib-java7:0.6.1` declarations from build files and version catalogs.
- [ ] Verify that no source references `ListSet` and that `javalib-java7` is absent from compile and runtime
  dependency graphs, including as an accidental transitive dependency.
- [ ] Preserve intentional Java and Scala interoperation or document source-breaking replacements.
- [ ] Remove compatibility shims only after all repository consumers migrate.
- [ ] Update public documentation and migration notes for all breaking API changes.

## Validation protocol

Use repository Gradle tasks from the repository root.

- [ ] During each phase, run the narrow compile and test tasks for touched modules.
- [ ] When Kotlin changes, run `./gradlew --parallel ktlintFormat` before final verification.
- [ ] When Scala in `alchemist-incarnation-scafi` changes, run
  `./gradlew --parallel alchemist-incarnation-scafi:scalafmtAll`.
- [ ] Re-run affected module verification after formatting.
- [ ] Finish every non-trivial completed change with `./gradlew --parallel build`.
- [ ] If the full build fails, record the exact blocker in this document and keep fixing until it passes or an
  external blocker is confirmed.

## Progress log

- 2026-08-02: created the living plan after the initial architecture, history, and compilation audit.
- 2026-08-02: made Java-to-Kotlin conversion mandatory whenever a Java file receives significant modification.
- 2026-08-02: required replacement of `ListSet` and removal of `org.danilopianini:javalib-java7:0.6.1` by the end
  of the refactor.
- 2026-08-02: began Phase 1 on `marmellata` at pre-merge `HEAD`
  `df3dea9fd518dc3c827283cbd1907a6f42e6c39d`. The worktree contained only this untracked plan, and the known
  build failures above remain the pre-merge baseline. Before refreshing, `origin/master` was
  `922a84e634d98c37779d423c5a76ec8927bc141c`.
- 2026-08-02: refreshed `origin/master`; it remains at
  `922a84e634d98c37779d423c5a76ec8927bc141c` (`ci(deps): update danysk/build-check-deploy-gradle-action action
  to v4.0.42 (#5524)`).
- 2026-08-02: started the normal merge of `origin/master`. Conflict resolution is in progress. Semantic conflicts
  are limited to SAPERE sources, an AIS loader test, and build/automation configuration; the remaining conflicts
  are generated Dokka/npm dependency artifacts or release metadata. The complete `dokka-cache` snapshot is taken
  exactly from `origin/master`, including its additions and deletions, rather than regenerated during the merge.
- 2026-08-02: resolved every merge conflict. Master supplies the newer build tooling, dependency versions,
  lockfiles, release metadata, and exact `dokka-cache` tree; the branch supplies reactive `tau`, position, and node
  count API usages. Branch-specific immutable-collection catalog aliases were retained alongside master's newer
  versions. No conflict markers or unmerged index entries remain.
- 2026-08-02: the merge commit hook reached `:alchemist-api:compileKotlin` and reproduced the known duplicate
  Java/Kotlin `TimeDistribution` declaration. Per project-owner direction, hooks are bypassed for this master merge;
  the failure becomes the first focused post-merge compilation frontier for Phase 2.
