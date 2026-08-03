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
- Make each `Neighborhood` value an immutable snapshot. Topology may change during a simulation, but a change must
  create and publish a replacement neighborhood rather than mutate an already published instance.
- Keep changes small and focused, and maintain this document as part of each phase.
- At every relevant, independently committable milestone, stop before beginning the next one and suggest a
  Conventional Commit message for the completed work.

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
10. Published neighborhoods and their neighbor collections are immutable; topology updates atomically replace the
    observable neighborhood snapshot.

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
- [x] Merge the current `origin/master` and refresh this evidence after conflict resolution.

Known failures after the master merge:

- Java and Kotlin declare incompatible `it.unibo.alchemist.model.TimeDistribution` interfaces.
- `AbstractEnvironment` mixes immutable `List` declarations with mutation and stale `ListSet` APIs.
- `SimpleNeighborhood` contains an unfinished `ImmutableL` type and implements methods removed from the new API.
- `Neighborhood` consumers still require collection, iteration, membership, and copy-update semantics.
- Quality checks report formatting in `ConnectToAccessPoint`, missing documentation in `PhysicsUpdate`, and an
  empty initializer in `SendToNeighbor`.

## Phase 1: merge master on the current branch

- [x] Refresh `origin/master` immediately before the merge.
- [x] Record the pre-merge `HEAD`, current build failures, and worktree status.
- [x] Merge `origin/master` into `marmellata` with a normal merge commit.
- [x] Resolve conflicts by preserving the reactive direction while adopting current master fixes and APIs.
- [x] Do not regenerate or commit caches, documentation caches, build outputs, or unrelated lock-file changes.
- [x] Re-run focused compilation to establish the post-merge failure frontier.
- [x] Update the current-evidence section with the merge commit and new failures.

## Phase 2: restore a compiling baseline

This phase restores coherence without prematurely finalizing the new scheduling API.

- [x] Keep exactly one `TimeDistribution` declaration. Retain the currently implemented Java reactive contract
  temporarily if that minimizes churn before Phase 4.
- [x] Complete or locally roll back the unfinished `Environment`/`Neighborhood` collection migration.
- [x] Replace encountered `ListSet` uses with Kotlin collections or immutable collections; do not introduce new
  uses of `javalib-java7` while restoring compilation.
- [x] Preserve ordered uniqueness for nodes, global reactions, and neighborhood members.
- [x] Represent neighborhoods as immutable, copy-on-write snapshots and replace the observable value when topology
  changes.
- [ ] Restore collection/iteration behavior required by Java, Kotlin, and Scala consumers.
- [ ] Fix the known formatting and static-analysis failures without suppressions.
- [x] Compile `alchemist-api`, `alchemist-engine`, and `alchemist-implementationbase` before widening verification.
- [ ] Record newly exposed downstream compilation failures here.

Current repository-wide Phase 2 frontier from `./gradlew --parallel build`:

- [x] Replace stale `ListSets.emptyListSet()` test fixtures with immutable Kotlin collections.
- [x] Fix eight `MapGetWithNotNullAssertionOperator` findings in `ObservableTest` without suppressions.
- [x] Make reaction invalidation safe before an environment is attached to a simulation; the failing regression is
  `TestReactiveDependencies.local reactions on separate nodes should be isolated`.
- [x] Trace and fix the website-snippet simulation failure `Propensity cannot be NaN`.
- [x] Fix the deterministic cognitive-agent social-contagion regression: the non-exposed pedestrian never begins
  evacuating under reactive scheduling.
- [x] Update ten biochemistry Java test assertions to read the current value of observable validity and propensity.
- [x] Update three Protelis Java test assertions to use the `TimeDistribution.getRate()` accessor.
- [x] Remove the known empty initializer from `SendToNeighbor`.
- [x] Fix method ordering and the SpotBugs finding in transitional `AbstractDistribution`.
- [x] Update `TestBiomolLayer` to compare against the current reactive execution-validity value.
- [x] Remove empty dependency loops and obsolete serialization members from SAPERE action/condition bases, and
  restore constructor grouping in `SAPEREGradient`.
- [x] Prevent SAPERE reactions with neighbor outputs from executing a cached match after its consumed LSA has been
  invalidated, and classify the accompanying SAPERE SpotBugs report for Phase 8.
- [x] Initialize the map-walker reaction explicitly in its direct-execution test fixture before advancing it.
- [x] Replace the loading regression's whole-environment Java serialization assertion with a load-only incarnation
  check, consistently with the accepted removal of `Serializable` from the live model graph.
- [x] Fix the node-removal lifecycle regression where the engine advances a reaction that was never reactively
  initialized (`alchemist-full` `TestRemoveNode`).
- [ ] Pull forward a bounded Phase 8 slice required by verification: remove serialization inheritance from action,
  condition, linking-rule, and speed-strategy APIs, porting the significantly changed Java interfaces to Kotlin.

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
- [ ] Remove serialization from the action, condition, linking-rule, and speed-strategy interface hierarchies.
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
- [ ] Test that previously published neighborhood snapshots cannot change when topology changes and that observers
  receive a distinct immutable replacement snapshot.
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

- 2026-08-03: added a workflow checkpoint at every relevant, independently committable milestone. Each checkpoint
  pauses further implementation and proposes a Conventional Commit message before work continues.
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
- 2026-08-02: completed Phase 1 with merge commit
  `59d06c9d52e6f4c802a3deb4726a0789f7e37ef9`, whose parents are the pre-merge branch head and the refreshed
  `origin/master`. The post-merge worktree was clean, and focused compilation stops at the duplicate
  `TimeDistribution` declarations as expected.
- 2026-08-02: removed the stale duplicate Kotlin `TimeDistribution`; the Java declaration remains temporarily
  because it is the contract consumed by the current reactive implementation. Both `:alchemist-api:compileKotlin`
  and `:alchemist-api:compileJava` now pass, exposing implementation-base collection migration as the next
  compiler frontier.
- 2026-08-02: `alchemist-engine` Kotlin and Java compilation pass. `alchemist-implementationbase:compileKotlin`
  now fails exclusively in the incomplete `Environment`/`Neighborhood` collection migration: stale `ListSet`
  types, a malformed `SimpleNeighborhood`, immutable `List` values used as mutable collections, and consumers
  expecting `Neighborhood` to provide iteration, membership, size, and copy-update operations.
- 2026-08-02: completed the core collection migration. `AbstractEnvironment` uses insertion-ordered sets internally
  and immutable-list snapshots externally; `SimpleNeighborhood` uses a deduplicated persistent list and exposes
  iterable, membership, size, and copy-update behavior to Kotlin, Java, and Scala. Mechanical Java callers now use
  `List`, ordered-set copies, and Java accessor syntax. `alchemist-implementationbase` Kotlin and Java compilation
  pass; no new `javalib-java7` use was introduced.
- 2026-08-02: made neighborhood immutability explicit: a neighborhood is an immutable topology snapshot, and a
  simulation topology change publishes a replacement snapshot rather than mutating existing neighborhood data.
- 2026-08-02: strengthened `Neighborhood.neighbors` to the immutable-collection type in the public API. The
  repository-wide Kotlin formatter compiled the migration successfully through Java, Kotlin, Scala, physics,
  maps, loading, and biochemistry before exposing four mechanical Java-getter fixes in `SAPEREReaction` as the
  next compiler frontier.
- 2026-08-02: replaced the stale SAPERE Kotlin-property syntax with Java `getRate()` calls. Focused SAPERE Kotlin
  and Java compilation now pass. The remaining bridge-method warnings are not suppressed and will disappear when
  reaction/time-distribution APIs are finalized.
- 2026-08-02: the first post-migration `./gradlew --parallel build` passed all production compilation and failed in
  five tasks: `alchemist-api:detektTest` (eight map-access findings), `alchemist-engine:test` (pre-simulation
  reactive invalidation), `alchemist-implementationbase:compileTestKotlin` (stale `ListSets` fixtures), root `test`
  (a website simulation reports `Propensity cannot be NaN`), and `alchemist-cognitive-agents:test` (one social-
  contagion movement assertion). These are the active Phase 2 frontier above.
- 2026-08-02: migrated `SimpleNetworkArrivals` test fixtures to `persistentListOf()` and replaced unsafe test-map
  assertions with `getValue`. `:alchemist-implementationbase:compileTestKotlin` and `:alchemist-api:detektTest`
  both pass without suppressions.
- 2026-08-02: made reactions retain their last known simulation time, allowing dependency invalidation after
  explicit initialization but before engine attachment. The reactive-dependency test now creates a fresh model for
  every source/dependency relation so a prior case cannot exhaust the next case's reactants; its diagnostics name
  both source and target. The focused engine regression passes.
- 2026-08-02: isolated the website-snippet failure to `variables-export.yml`. A disabled Protelis send reaction
  combined an infinite base rate with a zero condition contribution, yielding `Infinity * 0 = NaN`. Chemical
  reactions now short-circuit zero contributions before multiplication, and snippet failures include the resource
  name for actionable diagnostics. `./gradlew :test --tests 'TestWebsiteCodeSnippets'` passes.
- 2026-08-02: reran `TestFeelsTransmission` in isolation. The social-contagion case fails identically while the
  layer-driven case passes, so this is a deterministic reactive-invalidation regression rather than a stochastic
  fluctuation. The non-exposed pedestrian remains near its start instead of reacting to propagated danger.
- 2026-08-02: traced the cognitive regression to `WantToEscape`, which wraps the initial cognitive decision in a
  constant observable. Implementation is in progress to expose the escape decision as model-owned reactive state
  and publish transitions after cognitive updates.
- 2026-08-02: `CognitiveModel` now exposes an observable escape decision, `ImpactModel` publishes decision changes
  after each cognitive update, and `WantToEscape` consumes that live state. Both focused `TestFeelsTransmission`
  cases pass, including social contagion.
- 2026-08-02: all known focused Phase 2 regressions are resolved. Kotlin formatting and the repository-wide build
  are in progress to discover any remaining collection, interoperability, or static-analysis issues.
- 2026-08-02: Kotlin formatting passes. The next full build passes production compilation and exposes three masked
  downstream issues: ten biochemistry Java assertions compare a scalar to `Observable<Double>`, three Protelis Java
  assertions use `.rate` on a Java-declared distribution, and Detekt rejects `SendToNeighbor`'s empty initializer.
- 2026-08-02: corrected all three downstream issues. Focused biochemistry and Protelis test compilation plus
  Protelis Detekt pass. A clean formatting/build cycle is next.
- 2026-08-03: the clean full build passes all executed tests, including website snippets and cognitive social
  contagion. It now fails only `alchemist-implementationbase` verification: one Checkstyle overload-order finding
  and one SpotBugs finding in the transitional `AbstractDistribution` implementation.
- 2026-08-03: the `AbstractDistribution` overload order is fixed and its public `tau` view no longer exposes the
  mutable backing observable. The remaining SpotBugs report is serialization fallout from already making
  `Molecule` non-serializable plus an exposed mutable condition-dependency set. A bounded serialization-removal
  slice is in progress rather than restoring serialization or suppressing the findings.
- 2026-08-03: ported `Action`, `Condition`, `LinkingRule`, and `SpeedSelectionStrategy` from Java to Kotlin while
  removing their serialization inheritance; removed now-obsolete serialization fields and suppressions encountered
  in this slice. `AbstractCondition` now returns a defensive dependency-set copy. Core API/implementation
  compilation, implementation-base Checkstyle, and SpotBugs pass; repository-wide formatting and build remain.
- 2026-08-03: the repository formatter exposed one Kotlin/Java synthetic-property compatibility site in
  `WantToEscape`; it now calls the inherited `getNode()` method explicitly. No other module failed compilation
  before Gradle stopped at this frontier.
- 2026-08-03: the next formatter pass reached Protelis and found only named-argument compatibility warnings in
  `SendToNeighbor.cloneAction`; its parameter names now match the newly ported `Action` contract.
- 2026-08-03: the full build passed all executed tests and exposed four Checkstyle findings in two biochemistry
  actions where dependency-graph removal had left empty loops. The loops, unused imports, and obsolete
  serialization identifiers have been removed.
- 2026-08-03: the focused biochemistry Checkstyle rerun found one import-separator blank line left by that cleanup;
  it has been removed.
- 2026-08-03: the next repository build reached physics Detekt after all earlier checks passed and exposed the known
  missing documentation on `PhysicsUpdate.environment`. The public property is now documented; focused physics
  Detekt and repository-wide Kotlin formatting pass. The final repository-wide build is next.
- 2026-08-03: the full build cleared the previous static-analysis frontier and all other completed tests, then found
  one remaining biochemistry assertion comparing a Boolean to the observable returned by `canExecute()`. The
  assertion now reads the observable's current value, and the focused `TestBiomolLayer` run passes. The final
  repository-wide build is being rerun.
- 2026-08-03: the rerun confirms the biochemistry suite passes and exposes only SAPERE Checkstyle findings: two
  empty dependency loops left by graph removal and a loader-compatibility constructor separated from the primary
  constructor. The empty loops and obsolete serialization members are removed, and constructors are regrouped;
  focused SAPERE Checkstyle passes. The final repository-wide build is being rerun.
- 2026-08-03: SAPERE Checkstyle, Detekt, compilation, and the biochemistry regression remain green. The full run now
  reaches `RegressionTest.reactions with neighbor outputs should execute`, where a reaction attempts to remove a
  cached `<token>` match after another reaction consumed it. The same run generated a SAPERE SpotBugs report; both
  are the active Phase 2 frontier.
- 2026-08-03: added reaction identity and cached-match context to the stale SAPERE removal failure so the focused
  regression can identify which reactive subscription or scheduling transition failed before changing behavior.
  It identifies the stale reaction as `<token> --> <firing>`; dependency identity, current value, and observer count
  are now included to distinguish a missing subscription from a missed scheduler update.
- 2026-08-03: the stale reaction's molecule-name observable has two live observers, confirming scheduling wiring is
  present. Root cause is value aliasing: `observeMoleculeName` initially stored the mutable backing list, so removal
  mutated the observable's current value before equality-based emission and suppressed invalidation. Its initial
  value is now a defensive snapshot, and the temporary diagnostic wrapper has been removed.
- 2026-08-03: focused `RegressionTest.reactions with neighbor outputs should execute` passes with the defensive
  molecule-name snapshot. Focused SAPERE SpotBugs is next to classify the generated report before another full
  build.
- 2026-08-03: focused SAPERE SpotBugs completes successfully at the Gradle level; its findings are the expected
  broader serialization-removal inventory assigned to Phase 8, not a Phase 2 blocker. Four suppressions and serial
  identifiers on action classes became obsolete when `Action` stopped being serializable and have been removed.
- 2026-08-03: the SAPERE verification rerun found one separator blank left by suppression removal in
  `LsaRandomNeighborAction`; it has been removed before repeating the focused checks.
- 2026-08-03: focused SAPERE Checkstyle and SpotBugs tasks now complete successfully, and the obsolete-suppression
  findings are gone. Remaining SpotBugs findings are recorded serialization/API inventory for Phase 8. The final
  repository-wide build is being rerun.
- 2026-08-03: the next full-build frontier consists of eight map-walker tests directly advancing an uninitialized
  reaction and one loading regression that still requires whole-environment Java serialization. The map fixture
  will adopt the reactive lifecycle; the loading test will retain construction/incarnation coverage without
  restoring the serialization contract that Phase 8 intentionally removes.
- 2026-08-03: `TestTargetMapWalker` now completes the reaction's reactive initialization after installing it in the
  environment. All eight focused map-walker tests pass.
- 2026-08-03: `TestRegressions` now verifies environment construction and incarnation availability without Java
  object-stream round-tripping. Both focused loading regressions pass, consistently with the planned removal of
  `Serializable` from the live model graph.
- 2026-08-03: repository-wide `./gradlew --parallel ktlintFormat` passes after the map fixture migration. The
  mandatory full build is next; Phase 2 remains open until that run succeeds.
- 2026-08-03: the full build clears the prior map, loading, SAPERE, biochemistry, website, and static-analysis
  frontiers, then fails only `alchemist-full` `TestRemoveNode`: after a node-removal transition, the engine attempts
  to advance an `Event` whose reactive initialization never completed. This lifecycle defect is the remaining
  Phase 2 frontier.
- 2026-08-03: traced `TestRemoveNode` to the firing reaction removing and disposing its own node during
  `execute()`. The engine's normal post-fire advancement then observes a terminal disposed reaction, not an
  uninitialized live reaction. Advancement after disposal must therefore be inert while advancement before
  initialization remains an error.
- 2026-08-03: `AbstractReaction.update` now ignores advancement after terminal disposal without weakening the
  initialization check for live reactions. Focused `alchemist-full` `TestRemoveNode` passes.
