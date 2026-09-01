# Reactive Engine Refactor Plan

Last updated: 2026-09-01

Working branch: `marmellata`

This is the living execution plan for the graphless reactive-engine refactor. Update it whenever work starts or
finishes, an architectural decision changes, a new blocker is discovered, or validation produces new evidence.
Do not mark an item complete until its implementation and proportional verification are complete.

## Fixed decisions

- Continue on `marmellata`; do not restart the refactor on a new branch.
- Merge `origin/master` into `marmellata`; do not replace the existing work with a replay from scratch.
- Remove the dependency graph and all dependency-descriptor APIs used only to maintain it.
- Remove `BatchEngine` and converge on one execution and scheduling model.
- Remove `BatchManager`; a `nextOccurrence` emission must update the scheduler directly. Future invalidation
  transactions will deduplicate reaction recomputation before occurrence emission, rather than coalesce scheduler
  callbacks afterward.
- Make `TimeDistribution` responsible only for generating correctly distributed time samples.
- Make reactions own `nextOccurrence`, execution advancement, invalidation, and any transformation or replacement
  of a previously sampled time.
- Make `ReactionHost<T>` the single model-membership abstraction implemented by both `Node` and `Environment`.
  Hosts add and remove root `Reaction<T>` values and notify the engine exactly once, after membership actually
  changes. The engine owns only scheduler entries and scheduling subscriptions; it never dispatches on concrete
  reaction types or model-owner types.
- Keep `Node` and `Environment` non-iterable. Consumers use the explicit `Node.reactions`,
  `Environment.reactions`, and `Environment.nodes` collections so the iterated relationship is never ambiguous.
- Normalize scheduled-entity terminology in one atomic migration before later phases build more APIs on the current
  names: rename `Actionable<T>` to the owner-neutral root `Reaction<T>`, rename the current node-owned
  `Reaction<T>` to `NodeReaction<T>`, remove the empty environment-reaction marker in favor of environment host
  membership, and rename the
  current concrete `Event<T>` to `GenericReaction<T>`. The former absolute-time `Trigger` distribution is replaced
  by two one-shot reaction policies. `AbsoluteEvent<T>` owns a fixed occurrence, checks conditions only at that time,
  applies actions when they are valid, and removes itself in either validity outcome. `ConditionalEvent<T>` is a
  node-owned `GenericReaction<T>` specialization: it draws when conditions become valid, discards the putative
  occurrence on invalidation, redraws on the next invalid-to-valid transition, and removes itself after firing.
  Neither policy exposes removal as a mutable action. Do not use an event for a persistent, repeatedly scheduled
  generator or `BaseReaction` for a concrete reaction.
- Put residual-time reuse behind `AbstractMarkovianNodeReaction`. It requires an `ExponentialTime` and fails fast for
  every other distribution; `ChemicalNodeReaction` and its biochemical subclasses inherit this contract.
- Keep SAPERE reactions outside the Markovian hierarchy. They redraw after initialization, firing, and invalidation
  for every supported delay generator, while exponential draws still account for total match propensity.
  Absolute-time SAPERE programs use `AbsoluteEvent` rather than a special distribution branch.
- Model every Protelis send as an ordinary condition-gated `GenericReaction`, independently of the configured time
  distribution. Until `ComputationalRoundComplete` becomes true, the send exposes `Time.INFINITY` and consumes no
  sample. The false-to-true transition starts one wait from the current simulation time; firing sends once and
  resets validity to false. Remove both `ProtelisScheduledReaction` and the exponential-send `ChemicalNodeReaction`
  special case after base validity-gated scheduling is in place.
- During Phase 5, rename reaction `tau` to `nextOccurrence` across the API and repository consumers.
- Remove propensity contribution from the general condition API. A `Condition` describes reactive validity, not a
  numeric scheduling factor. Each specialized reaction family validates the concrete condition types or semantic
  condition capabilities it accepts and computes its own scheduling law from their typed state. Do not replace
  `getPropensityContribution()` with another universal scalar-contribution interface.
- Make unsatisfied scheduling conditions suspend scheduling: whenever their combined validity is `false`, the
  reaction's public `nextOccurrence` is `Time.INFINITY`. Revalidation applies the reaction family's invalidation
  policy at the current simulation time before publishing a finite occurrence; it must never resurrect an
  occurrence in the past. `AbsoluteEvent` conditions are occurrence-time guards instead: they never alter its fixed
  occurrence and are read exactly once when that occurrence is selected.
- Remove `Context` locality metadata from reactions, actions, and conditions. Reactive invalidation is expressed by
  exact observable inputs, not broad local, neighborhood, or global categories. Do not introduce a replacement
  locality-capability API for scheduling optimizations.
- Keep node movement and neighborhood maintenance entirely inside the model. The simulation boundary accepts only
  root reaction membership notifications for scheduling: adding or removing a node expands to one ordinary
  `reactionAdded` or `reactionRemoved` notification per hosted reaction.
- Remove observable dependency sets from conditions after migrating every non-validity scheduling input to a
  direct reaction-specific invalidation signal.
- Make layers observable before completing that migration. Consumers of layer data must receive changes through a
  typed observable layer-value contract and exact owned subscriptions, not by publishing the layer through a
  condition dependency set.
- Put chemical propensity computation in specialized reactions. Their propensity and match invalidation signals
  are private reactive implementation details, not a new public token hierarchy, dependency-description API, or
  generic `PropensityCondition`. A chemical reaction may, for example, accept molecule-quantity/presence conditions
  and derive mass action from their molecule, required quantity, and observed concentration itself; other reaction
  families explicitly define different accepted condition sets.
- Renounce Java `Serializable` across the model and engine APIs.
- When a Java source file requires significant behavioral, structural, or API modification, port that file to
  Kotlin as part of the same change instead of extending the Java implementation.
- Eliminate the `org.danilopianini:javalib-java7:0.6.1` dependency by the end of the implementation. Replace
  `ListSet` with suitable Kotlin collection types or immutable collections while preserving required ordering and
  uniqueness semantics.
- Make each `Neighborhood` value an immutable snapshot. Topology may change during a simulation, but a change must
  create and publish a replacement neighborhood rather than mutate an already published instance.
- Keep changes small and focused, and maintain this document as part of each phase.
- Treat documentation consistency as part of every change's definition of done. Whenever a change affects behavior,
  API, architecture, configuration, or workflows described by existing KDoc, Javadoc, website pages, READMEs, or
  migration notes, update every affected document in the same change and verify that no stale terminology or claims
  remain.
- Write website documentation as a timeless description of the current simulator. State the implemented concepts,
  responsibilities, behavior, and invariants directly; omit removed mechanisms, absent hooks, and comparisons with
  previous architectures. Reserve change-oriented explanations for release notes, migration guides, and explicitly
  historical material.
- Link every website reference to a public Alchemist type with the Hugo `api` shortcode, including its package when
  useful for disambiguation. Reserve inline code formatting for members, configuration keys, values, and source
  fragments rather than rendering class names as plain code.
- Keep model-level documentation incarnation-neutral. Incarnation-specific types and scheduling policies belong in
  their incarnation sections, linked from shared model pages only when navigation requires it.
- Keep repository-wide validation focused on the default JVM and normal verification suites. Exclude alternate-JVM
  test matrices, DEB/RPM packaging and RPM-derived metadata, and fat/shadow JAR construction and tests.
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
    +--> reactive validity -->| Reaction (root)         |
    |                         | - owns nextOccurrence   |--> observable nextOccurrence --> Engine --> Scheduler
    +--> private reaction ---->| - owns invalidation     |
         invalidation signal  |                         |
                              | - owns time adjustment  |
TimeDistribution ------------>| - requests new samples |
(generates time samples only) +-------------------------+
```

The engine must not infer model dependencies. It schedules reactions and reindexes them when their owned
`nextOccurrence` changes. Model observables invalidate the reactions that directly consume them.

The scheduled-entity type hierarchy will be:

```text
Reaction
├── NodeReaction
└── TimeDistributedReaction

TimeDistributedReaction contains recurrence-only timeDistribution and rate metadata.

AbstractReaction
├── AbsoluteEvent
└── AbstractNodeReaction : NodeReaction
│   ├── GenericReaction
│   │   └── ConditionalEvent
│   ├── AbstractMarkovianNodeReaction
│   │   └── ChemicalNodeReaction
│   └── other node-owned specializations

ReactionHost
├── Node
└── Environment
```

The root `Reaction` contract and future `AbstractReaction` contain only owner-neutral scheduling, condition,
execution, and subscription behavior. `NodeReaction` and `AbstractNodeReaction` own node association and cloning
onto another node. `ReactionHost` owns reaction membership and forwards successful membership changes to the engine.
The engine and scheduler consume only the root `Reaction`; node-specific actions, conditions, and clone APIs consume
`NodeReaction`. Environment ownership needs no marker because membership in the environment host is authoritative.

`AbsoluteEvent` is a host-neutral one-shot `AbstractReaction`, not an interface or a `TimeDistribution`. It owns one
absolute occurrence and a `ReactionHost`; at that occurrence it executes actions only if its conditions are valid,
then removes itself from the host in either validity outcome. `ConditionalEvent` is a node-owned one-shot
`GenericReaction`: each enabling transition draws a fresh putative occurrence, invalidation discards it, and
successful execution removes the event from its node. The host removes model membership first and then issues the
ordinary engine `reactionRemoved` notification, so scheduler and subscription cleanup require no event-specific
engine path. Absolute events are not cloned with nodes; conditional events implement the normal `NodeReaction`
fresh-clone contract.

### Architectural invariants

1. A `TimeDistribution` does not know about `Reaction`, `Environment`, conditions, propensity,
   scheduler state, or observable `nextOccurrence`.
2. Drawing a new sample is observably different from adjusting an already sampled occurrence. A reaction decides
   which operation is semantically correct.
3. A reaction owns its absolute next-occurrence time and is the only component allowed to change it.
4. A reaction whose conditions gate scheduling and whose combined validity is `false` exposes `Time.INFINITY` as
   `nextOccurrence`. A false-to-true transition refreshes reaction-specific state and applies that reaction family's
   revalidation policy at the current simulation time before a finite occurrence is published. Absolute-event
   conditions are checked only at their fixed occurrence and do not gate scheduler visibility.
5. The engine observes reaction times but never computes them. An infinite scheduler head denotes quiescence and is
   not consumed as a firing, skipped execution, post-firing update, monitor step, or step-count increment.
6. A completed one-shot event removes itself from its host. A conditional event completes after successful action
   execution; an absolute event completes after its occurrence-time condition check, whether or not actions ran. The
   host notifies the engine exactly once after membership removal; the engine applies the same
   scheduler/subscription cleanup as for every reaction.
7. Conditions expose validity only through the general contract. They expose neither scheduling policy, a numeric
   propensity contribution, nor a general collection of observable dependencies.
8. Each specialized reaction family defines and validates the condition types or semantic condition capabilities it
   accepts. Unsupported conditions fail at construction or assignment, before initialization and scheduling.
9. A specialized reaction directly observes the private invalidation signals needed by its scheduling law. A
   chemical reaction must therefore react to every value used by its propensity law, including changes that leave
   Boolean condition validity `true`. These signals are ordinary reactive sources, not dependency descriptors or
   instances of a new general-purpose token API.
10. Each logical model mutation transaction recomputes and reindexes every affected reaction at most once.
11. Removed or disposed reactions cannot emit scheduler updates.
12. All scheduling calculations use the triggering transition's simulation time, never a stale batch/global time.
13. Published neighborhoods and their neighbor collections are immutable; topology updates atomically replace the
    observable neighborhood snapshot.

### Soundness constraints to settle before the core API is finalized

- Require distributions to sample non-negative delays; reactions convert delays to absolute `nextOccurrence`.
  Represent a one-shot absolute occurrence as an `AbsoluteEvent`, not as a `Trigger` distribution. Consequently, move
  `timeDistribution` and recurrence-specific `rate` reporting off the owner-neutral root `Reaction` contract if an
  event has no meaningful sampler or rate.
- Define each specialized reaction's propensity or match inputs precisely. Prefer typed derived observables or
  narrow change signals owned by that reaction family; do not introduce an opaque token hierarchy that recreates
  the old dependency metadata.
- Expose semantic condition state needed by an accepting reaction—such as molecule identity, required quantity,
  observed concentration, matching candidates, or valid neighbors—rather than a precomputed generic propensity
  scalar. Validate accepted condition types when conditions are installed, and preserve that validation through
  cloning, builders, reflection, YAML loading, and Kotlin DSL construction.
- Apply adjustment semantics per reaction family. `AbstractMarkovianNodeReaction` rescales a surviving exponential
  residual without drawing; the base generic policy and SAPERE explicitly redraw after invalidation. An
  `AbsoluteEvent` preserves its fixed occurrence and never enters recurring adjustment logic. A `ConditionalEvent`
  discards its putative occurrence on invalidation and draws again only on the next enabling transition.
  `ChemicalNodeReaction` rejects non-exponential distributions at construction rather than silently changing policy.
- [x] Define absolute-event invalidity as expiry: conditions are sampled only at the fixed occurrence, actions run
  only if all are valid, and the event removes itself in either validity outcome. Define conditional-event
  invalidation separately: discard the putative occurrence, expose infinity, and draw again on the next
  invalid-to-valid transition.
- Decide whether making the root `Reaction` interface open is the intended public extensibility contract. The
  cross-module `AbstractReaction` directly implements it and therefore cannot retain the previous sealed declaration.
- Treat the Protelis send gate as validity, not as a specialized scheduling input. The first false-to-true
  `ComputationalRoundComplete` transition schedules a `GenericReaction` with the configured distribution; repeated
  true emissions do not redraw or postpone the pending send. `SendToNeighbor` resets the gate to false when it
  fires, so post-firing advancement must not sample again until the next completed computational round.
- Test random-number consumption as part of each invalidation contract.

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
- [x] Audited current `HEAD` `a25805497` on 2026-08-17; the worktree is clean and the branch matches
  `origin/marmellata`.
- [x] Confirmed that the old `Dependency` descriptor API and general lifecycle package are absent, while exact
  `Disposable` subscription handles and reaction/engine ownership remain in place.
- [x] Record post-`a25805497` formatting, focused verification, and full-build evidence.

Historical failure list from the post-merge baseline (not current evidence):

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
- [x] Restore collection/iteration behavior required by Java, Kotlin, and Scala consumers.
- [x] Fix the known formatting and static-analysis failures without suppressions.
- [x] Compile `alchemist-api`, `alchemist-engine`, and `alchemist-implementationbase` before widening verification.
- [x] Record newly exposed downstream compilation failures here.

Current repository-wide Phase 2 frontier from `./gradlew --parallel build`:

- [x] Replace stale `ListSets.emptyListSet()` test fixtures with immutable Kotlin collections.
- [x] Fix eight `MapGetWithNotNullAssertionOperator` findings in `ObservableTest` without suppressions.
- [x] Make reaction invalidation safe before an environment is attached to a simulation; the failing regression is
  `TestReactiveDependencies.local reactions on separate nodes should be isolated`.
- [x] Trace and fix the website-snippet simulation failure `Propensity cannot be NaN`.
- [x] Cover social contagion with a zero-local-danger pedestrian that sees an exposed peer and begins evacuating.
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
- [x] Pull forward a bounded Phase 8 slice required by verification: remove serialization inheritance from action,
  condition, linking-rule, and speed-strategy APIs, porting the significantly changed Java interfaces to Kotlin.

## Phase 3: remove `BatchEngine`

- [x] Inventory the complete batch-engine surface before deletion and record the migration boundary.
- [x] Delete `BatchEngine` and its output replay strategy.
- [x] Audit `BatchedScheduler`, fixed-batch and epsilon-batch queues, constructors, loaders, configuration strings,
  tests, examples, and documentation.
- [x] Remove batch-only scheduler types that no longer have a supported consumer.
- [x] Migrate any generally useful scheduler behavior to the single `Engine` path.
- [x] Remove batch-specific synchronization and stale-time accommodations.
- [x] Add a compatibility error for removed batch-engine configuration where silent fallback would be dangerous.
- [x] Verify that the remaining engine has one authoritative event-time and scheduler-update path.

## Phase 4: reduce `TimeDistribution` to sampling

- [x] Use Apache Commons Math's `RealDistribution` as the design reference: keep sampling independent from
  reaction lifecycle and scheduling, and add distribution metadata only when an identified consumer needs it.
- [x] Define the generic sampling contract as `TimeDistribution<T>.sample(): Time`, returning a finite non-negative
  delay. The distribution is sample-only with respect to scheduling and lifecycle; the intentional
  `newInstanceOn(node)` operation is a construction contract for fresh destination-bound generators.
- [x] Remove `update`, `reactToUpdate`, `Actionable`, `Environment`, propensity, rate-conditioning, and observable
  next-occurrence responsibilities from `TimeDistribution`.
- [x] Keep configured parameters such as an exponential lambda inside the sampler when they shape its probability
  law, but do not expose a generic `rate`: reactions own execution rate/propensity and any reporting of it.
- [x] Migrate deterministic, exponential, trigger, Weibull, network-arrival, molecule-controlled, and incarnation-
  specific implementations.
- [x] Make invalid samples, negative delays, NaN, and infinity behavior explicit.
- [ ] Add isolated deterministic and statistical tests for every distribution family.

## Phase 5: make reactions own scheduling

- [x] Give each reaction sole ownership of observable `nextOccurrence`.
- [x] Complete the replacement of shared-generator reaction cloning with **newly instantiated program** semantics:
  - make `TimeDistribution<T>.newInstanceOn(node)` construct a fresh, destination-bound generator with the same
    configuration; this open-world operation replaces the rejected central built-in factory registry;
  - construct a fresh `TimeDistribution` instance bound to the destination node and environment instead of passing
    the source reaction's generator object to the clone;
  - initialize a fresh reaction occurrence by sampling from the clone time (while respecting any configured
    absolute not-before/start time), without copying the source `nextOccurrence`, residual waiting time, previous
    propensity, match token, or other reaction-local sampler state;
  - allow reconstructed generators to keep using the simulation's shared RNG intentionally, without treating the
    generator object itself as shared reaction state;
  - explicitly rebind node-dependent generators such as `MoleculeControlledTimeDistribution` and
    `SimpleNetworkArrivals`, and isolate mutable specialized state such as SAPERE match tokens;
  - add cloning regressions for deterministic and non-memoryless generators, destination-node-dependent generators,
    RNG-backed generators, and specialized mutable generators.
  Execution checkpoints for this step:
  - [x] Inventory every reaction clone path and distinguish generator configuration from sampler/reaction runtime
    state.
  - [x] Reject the closed-world `builtInFactory()` approach: adding a distribution must never require modifying a
    central type switch.
  - [x] Reinstate generic `TimeDistribution<T>` with `newInstanceOn(node)` and implement it in every distribution;
    opaque nested samplers must retain their own reconstruction recipe or fail clearly when reinstantiation is
    requested.
  - [x] Make base reaction cloning use `newInstanceOn(node)` and initialize the fresh generator without copying a
    running schedule.
  - [x] Migrate cognitive, biochemical, SAPERE, and other specialized reaction clones; preserve shared simulation
    RNGs while isolating generator objects and mutable match/token state.
  - [x] Complete the deterministic, non-memoryless, destination-bound, RNG-backed, and specialized clone regression
    matrix.
- [x] Rename reaction `tau` to `nextOccurrence` across the API, implementations, engine, schedulers, loaders,
  tests, and documentation.
- [x] Define initialization, firing, invalidation, and removal transitions; cloning follows the newly-instantiated-
  program contract above rather than copying a running stochastic process.
- [x] Move all decisions about sampling, preserving, transforming, or replacing scheduled times into reactions.
- [x] Keep scheduler notification as a consequence of a reaction-owned `nextOccurrence` change.
- [x] Delete `BatchManager` and make each `nextOccurrence` emission call `scheduler.updateReaction` directly.
- [x] Ensure initialization computes `nextOccurrence` before scheduler insertion without causing duplicate
  reindexing.
- [x] Make disposal idempotent and prevent post-removal emissions.
- [x] Document and enforce simulation-thread confinement for scheduling registration and fail fast on registration
  errors.
- [x] Complete the scheduled-entity naming migration as one coherent public-API change:
  - [x] Rename `Actionable<T>` to the owner-neutral root `Reaction<T>` and move only behavior shared by node- and
    environment-owned reactions into it.
  - [x] Rename the current node-owned `Reaction<T>` to `NodeReaction<T>`, retaining `node` and
    `cloneOnNewNode(...)` only on this branch.
  - [x] Rename `GlobalReaction<T>` to the provisional `EnvironmentReaction<T>` while ownership was still encoded as
    a marker type; the later `ReactionHost` step below removes that marker.
  - [x] Rename the current concrete `Event<T>` to `GenericReaction<T>`. Do not introduce `BaseReaction`; use explicit
    `AbsoluteEvent<T>` and `ConditionalEvent<T>` names for the two one-shot scheduling policies.
  - [x] Remove `Trigger` from the `TimeDistribution` hierarchy. Construct an `AbsoluteEvent` from its occurrence
    directly, migrate reflective/YAML loading and SAPERE special cases, and keep delay sampling uniform for every
    remaining distribution. Construct `ConditionalEvent` with an ordinary `TimeDistribution`.
  - [x] Keep `nextOccurrence` on the root `Reaction`, but move mandatory `timeDistribution` and recurrence-specific
    `rate` reporting to distribution-backed reactions so an `AbsoluteEvent` does not expose meaningless sampler metadata.
    The temporary root post-occurrence hook below advances stateful samplers without requiring an engine type branch.
  - [x] Make successful event execution take the exact ordinary unregister path by removing itself from its host;
    the resulting notification disposes the engine-owned subscription and scheduler entry. Do not publish
    `Time.INFINITY` or retain an event-specific engine path.
  - [x] Normalize node and environment membership through `ReactionHost`, so removal changes model membership once,
    sends at most one engine notification, and cannot let later node cloning recreate a host-neutral event.
  - [x] Extract an owner-neutral `AbstractReaction` from the current node-specific `AbstractNodeReaction` and use it
    for both recurring node reactions and the host-neutral event.
  - [x] Replace the temporary rejection of event conditions with explicit policies: absolute events check once and
    expire; conditional events discard and redraw across disabling and enabling transitions.
  - [x] Replace the provisional `Event` and `EnvironmentReaction` interfaces with `ReactionHost`; make `Node` and
    `Environment` host root reactions and notify the engine only after actual membership changes. Make concrete
    `AbsoluteEvent : AbstractReaction` remove itself after its occurrence-time check, and make
    `ConditionalEvent : GenericReaction` remove itself after successful action execution.
  - [x] **TEMPORARY CONSTRAINT:** put `updateSchedulingAfterFiring(Time)` on the root reaction protocol and invoke it
    uniformly after every selected occurrence. This is required only while a condition-invalid reaction can still
    remain scheduler-visible at a finite time: skipped execution must consume that occurrence without engine type
    inspection. Its deletion is a separate explicit Phase 6 step; this is not the final firing protocol.
  - [x] Retype engine, scheduler, simulation, output-monitor, and extractor boundaries to the root `Reaction`;
    retype node collections, incarnations, actions, conditions, cloning, and node-oriented DSLs to `NodeReaction`;
    retype both model-membership branches to `ReactionHost` and root `Reaction`.
  - [x] Rename environment membership APIs from `environmentReactions`, `addGlobalReaction`, and
    `removeGlobalReaction` to the shared `reactions`, `addReaction`, and `removeReaction` host vocabulary. Keep the
    YAML `global-programs` spelling because it identifies placement rather than a reaction type.
  - [x] Migrate all Java, Kotlin, and Scala consumers atomically, including tests and generated/factory-facing API
    surfaces. Remove transitional aliases once repository consumers compile unless a deliberate compatibility
    contract is documented.
  - [x] Update KDoc/Javadoc, the metamodel, the dedicated scheduling-and-ownership page, engine boundary
    documentation, configuration references, diagrams, and migration notes in the same change. Historical entries
    may retain old names only when clearly identified as historical.
  - [x] Add or adapt regressions proving that the scheduler treats both ownership branches uniformly, runtime
    addition/removal disposes subscriptions for both branches, and cloning remains exclusive to node reactions.

## Phase 6: simplify conditions and introduce reaction-specific invalidation signals

- [x] Remove `Condition.getPropensityContribution()`, the propensity observable and setter from
  `AbstractCondition`, `AbstractNonPropensityContributingCondition`, and every condition-level override or helper.
- [x] Inventory every condition that currently publishes a propensity contribution. Classify each value as either
  redundant with Boolean validity or semantic state that a specific reaction family must consume directly; do not
  preserve the old scalar merely under a narrower generic interface.
- [x] Make the base reaction consume only condition validity and accept arbitrary conditions without inferring
  scheduling policy from their runtime types.
- [x] Give each specialized reaction family an explicit accepted-condition contract and validate the complete list
  whenever conditions are assigned. Reject unsupported types with a targeted error before initialization; do not
  silently treat them as a factor of one or defer failure until the first scheduling refresh.
- [x] Keep `ChemicalNodeReaction` as a condition-free Markovian policy base. Concrete chemical families define and
  validate their own typed condition sets and rate laws: `BiochemicalNodeReaction` accepts molecule quantity,
  neighborhood selection, extracellular-environment, and mechanical-tension conditions and computes their factors
  from typed state rather than weakening the base contract.
- [ ] Expose the narrow semantic observables required by those accepted types without exposing a generic numeric
  contribution. The owning specialized reaction subscribes to them and owns every returned `Disposable`.
  - [x] Expose biochemical molecule quantities, neighbor-selection weights, and mechanical state through typed
    condition APIs and compute the rate from them in `BiochemicalNodeReaction`.
  - [ ] Replace the remaining condition dependency-set subscriptions with direct specialized-reaction subscriptions.
    In particular, `BiomolPresentInEnv` still reads a layer synchronously because layer values are not observable.
- [ ] Preserve accepted-condition validation and typed bindings through reaction cloning, incarnation builders,
  reflection/YAML loading, generated factories, and Kotlin DSL construction.
- [x] Update `Condition` and specialized-reaction KDoc/Javadoc, the scheduling-and-ownership page, model and
  biochemistry documentation, configuration references, and migration notes in the same change. Remove every claim
  that a general condition contributes a numeric factor to reaction propensity.
- [ ] Classify every current condition dependency source as Boolean validity input or as a specialized reaction's
  non-validity scheduling input before removing the existing dependency sets.
- [ ] Make the base reaction observe only the combined reactive validity of its conditions.
- [x] Make reaction validity gate the public scheduling observable: combined validity `false` publishes
  `Time.INFINITY` immediately, including during initialization, without consuming an occurrence or advancing a
  stateful distribution.
- [x] On a false-to-true validity transition, refresh reaction-specific state and apply the reaction family's
  revalidation policy at the current simulation time before publishing a finite `nextOccurrence`. Never expose a
  stale internal candidate that lies in the past.
- [ ] Specify and test revalidation separately for generic redraw, Markovian memoryless sampling/rescaling, SAPERE
  match resampling, both one-shot event policies, and condition-gated Protelis sends. If a family retains suspended internal
  state, keep it private: only the validity-gated occurrence is public and scheduler-visible.
  - [x] Cover generic redraw, Markovian rate transitions, and the deterministic/stateful Protelis send sequence.
  - [x] Cover `ConditionalEvent` sample consumption, invalidation, re-enabling, and removal; cover
    `AbsoluteEvent` valid execution, invalid expiry, fixed scheduler visibility, and removal.
  - [ ] Complete SAPERE match-revalidation and exponential Protelis-send sample-count coverage.
- [x] Make the engine treat an infinite scheduler head as quiescence. It must not call `execute`, `reactionReady`,
  `updateSchedulingAfterFiring`, or `stepDone`, and must not increment the simulation step merely to discover that no finite
  reaction is available.
- [x] **EXCEPTIONAL PATH REMOVAL:** after validity-gated `nextOccurrence` and infinite-head quiescence are complete,
  remove the temporary selected-but-invalid path. Delete the engine's unconditional root
  `updateSchedulingAfterFiring` call and the temporary root hook; make each successful recurring `execute()` perform
  its own post-fire advancement, while one-shot event execution removes itself from its host. Add a regression proving that
  the engine performs only the uniform `Reaction.execute()` protocol and never advances a skipped or
  concrete-type-specific reaction.
- [ ] Make specialized reactions directly subscribe to the narrow propensity, match, or state-change signals they
  consume, owning every returned non-null `Disposable`.
- [ ] Audit observable disposal ownership while migrating those subscriptions. A condition or reaction disposes its
  own derived observables and subscription handles, but must not dispose a borrowed model observable; in particular,
  remove the shared-program disposal from `ComputationalRoundComplete` and add multiple-consumer removal regressions.
- [ ] Make these private signals invalidate the reaction even for semantically relevant `true`-to-`true` validity
  changes; do not encode such changes as repeated equal Boolean emissions.
- [ ] Until Phase 7 transactions deduplicate invalidation, inventory condition implementations that publish both a
  source and a derived observable through dependency sets; prevent one logical mutation from resampling or
  recomputing a reaction twice during the direct-subscription migration.
- [ ] Remove `Condition.getDependencies`, `AbstractCondition.dependencies`, `addObservableDependency`, and related
  merge helpers once all non-validity consumers have migrated.
- [x] Do not replace observable dependency sets with a public token type, token registry, or another collection of
  dependency descriptions.
- [x] Define the specialized chemical reaction type and its typed condition/state inputs.
- [ ] Cover every concentration, match, stoichiometric quantity, or neighborhood value required to recompute
  propensity.
- [x] Move mass-action and other propensity laws into the chemical reaction hierarchy.
- [x] Enforce `ExponentialTime` as the only valid generator for `ChemicalNodeReaction` and fail fast otherwise.
- [x] Migrate biochemistry and SAPERE scheduling explicitly: biochemical reactions inherit the chemical Markovian
  contract, while SAPERE reactions redraw outside that hierarchy and normalize exponential samples to total match
  propensity.
- [x] Replace the provisional Protelis send split with `GenericReaction` for every distribution. Remove
  `ProtelisScheduledReaction`; do not use `ChemicalNodeReaction` merely because a send's distribution is exponential.
- [x] Make a send initially invalid with `Time.INFINITY` and zero consumed samples. On the first false-to-true
  `ComputationalRoundComplete` transition, draw exactly once and schedule from the current simulation time.
- [x] Ensure repeated program completions while a send is already pending do not redraw or postpone it. When the
  send fires and `SendToNeighbor` resets completion to false, publish `Time.INFINITY` without drawing the next sample.
- [x] Replace the existing independent-cadence regression and update Protelis KDoc, the scheduling explanation, and
  every plan claim that currently describes skipped-occurrence advancement or cadence preservation.
- [ ] Reject missing, negative, NaN, or otherwise invalid propensity values at the reaction boundary.

## Phase 7: implement reactive invalidation transactions

- [ ] Introduce an engine-owned transaction/dirty-reaction set around each model mutation and reaction execution.
- [ ] Mark reactions dirty synchronously through direct validity and reaction-specific invalidation callbacks; the
  transaction must never inspect condition dependency metadata.
- [ ] Recompute each dirty reaction once after the mutation finishes.
- [ ] Reindex each changed `nextOccurrence` once as a consequence of the single post-transaction recomputation.
- [ ] Define behavior for invalidation originating from scheduled commands outside reaction execution.
- [ ] Guard observable callback iteration against subscription mutation and re-entrant emissions.
- [ ] Verify deterministic ordering when multiple reactions are invalidated together.
- [ ] Keep the invalidation transaction independent from scheduler notification: it must prevent redundant
  recomputation, resampling, and random-number consumption before `nextOccurrence` emits, not buffer scheduler
  callbacks after emission.
- [ ] Exclude reactions removed or disposed while a transaction is collecting dirty reactions.

## Phase 8: remove Java serialization

- [ ] Inventory `Serializable`, `serialVersionUID`, `@Serial`, `@Transient`, `readObject`, `writeObject`, and cloning
  code coupled to serialization.
- [x] Remove serialization from the action, condition, linking-rule, and speed-strategy interface hierarchies.
- [ ] Remove serialization inheritance from model, engine, reaction, condition, action, node, environment, molecule,
  and time-distribution APIs where present.
- [ ] Remove obsolete serialization fields, hooks, tests, and warning suppressions.
- [ ] Check exporters, UI tools, distributed execution, and loaders for accidental reliance on Java object streams.
- [ ] If persistence remains necessary, define explicit versioned DTOs outside the live reactive object graph.
- [ ] Document the breaking change.

## Phase 9: complete observable model migration

- [ ] Audit every condition and reaction for non-observable reads that affect validity, specialized invalidation,
  or scheduling.
- [ ] For every source formerly present in a condition dependency set, verify an explicit destination: condition
  validity or a specialized reaction-owned invalidation signal.
- [ ] Cover node contents, molecule presence, neighborhoods, positions, node counts, ranges, layers, and global state.
- [ ] Make layers observable:
  - [ ] Define a typed API for observing a layer's value at a position, including the semantics of static layers,
    mutable layer values, and disposal.
  - [ ] Compose layer-value observation with observable node positions so movement invalidates consumers even when
    the layer object itself does not change.
  - [ ] Decide whether layer association can change at runtime; if it can, expose the environment's layer registry
    as an observable map and define add, replacement, and removal semantics before exposing mutation operations.
  - [ ] Migrate every layer-reading condition, reaction, action, exporter, and incarnation to the observable
    contract. Scheduling consumers must own exact subscription handles or consume a lazy derived observable.
  - [ ] Add regressions for static layers, mutable layer values, movement across a spatial gradient, runtime layer
    association changes if supported, subscription disposal, and absence of updates after reaction or node removal.
  - [ ] Update the layer API documentation, YAML reference, layer how-to, and scheduling documentation in the same
    change, removing the current assumption that layers are necessarily static.
- [ ] Audit biochemistry, Protelis, SAPERE, Scafi, cognitive agents, physics, maps, and environment-owned reactions.
- [x] Remove topology-driven engine callbacks. Neighborhood and position changes remain observable model state;
  affected reactions publish their own scheduling changes through `nextOccurrence`.
- [x] Remove reaction-level input/output `Context` from the scheduled-reaction root, implementations, GraphQL,
  tests, and documentation.
- [x] Remove the remaining action/condition `Context` API and the SAPERE optimization that depended on it. SAPERE
  now conservatively recomputes its matches instead of inferring mutation scope from action categories.
- [x] Route runtime node addition and removal through the same per-reaction membership notifications used by direct
  host mutations, retaining exact scheduler-subscription cleanup.
- [x] Remove node, movement, and neighborhood callbacks from `Simulation` and `Engine`; topology recomputation no
  longer constructs add/remove operation records whose sole consumer was dependency invalidation.

## Phase 10: behavioral and regression coverage

- [x] Port old dependency-graph expectations as observable behavior tests, not graph-structure tests.
- [x] Test positive local invalidation and negative isolation across unrelated nodes.
- [x] Test invalid-at-initialization, true-to-false suspension, and false-to-true revalidation. Assert that invalid
  reactions are indexed at `Time.INFINITY`, never execute, never advance stateful distributions, and never generate
  phantom monitor notifications or step increments.
- [ ] Test that revalidation cannot publish a time earlier than the current simulation time and consumes exactly the
  random samples required by the reaction family's documented policy.
- [ ] Test the complete Protelis send sequence with both deterministic/stateful and exponential distributions:
  initial false validity means infinity and zero samples; the first completed program round draws once; repeated
  true notifications preserve the pending occurrence; send execution resets validity to false and infinity without
  another draw; the next completed round starts exactly one new wait.
  - [x] Cover the complete deterministic/stateful sequence with explicit sample counts.
  - [ ] Repeat the sequence with an exponential distribution and explicit random-generator consumption.
- [ ] Test neighborhood addition/removal, movement, environment-wide changes, and dynamic reaction/node changes.
- [ ] Test that previously published neighborhood snapshots cannot change when topology changes and that observers
  receive a distinct immutable replacement snapshot.
- [ ] Test propensity changes that do not change Boolean condition validity.
- [ ] Test that the general `Condition` API exposes no propensity contribution, supported chemical condition types
  drive the reaction-owned mass-action law, and unsupported conditions fail during assignment/loading.
- [ ] Test specialized condition acceptance and propensity behavior across chemical, biochemical, SAPERE, and other
  families migrated from condition-level contributions, including clone reconstruction and exact subscription
  cleanup.
- [ ] Test match and other specialized scheduling changes that leave condition validity `true`, proving that their
  direct reaction-specific signals invalidate scheduling without dependency sets or repeated Boolean emissions.
- [ ] Test zero-to-positive, positive-to-zero, and positive-to-positive chemical propensity transitions.
- [ ] Verify whether each transition preserves, transforms, or redraws the sampled time as specified.
- [ ] Assert random-number consumption explicitly for stochastic generators.
- [ ] Test one recomputation and scheduler reindex per logical transaction.
- [ ] Test observer cleanup and absence of post-removal scheduler updates.
- [ ] Test that node- and environment-owned events execute at most once and then disappear from the scheduler,
  engine subscription ownership, and model owner without an infinite reindex, phantom step, or clone resurrection.
- [ ] Test and document the selected policy for an event whose condition is invalid at its absolute occurrence.
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
- [ ] Remove the remaining Swing UI `org.danilopianini:javalib-java7:0.6.1` dependency.

## Phase 12: audit documentation coherence

- [x] Rewrite the engine overview, metamodel overview, and reaction-scheduling page as descriptions of the current
  architecture, removing change-history narration and links whose purpose was to explain superseded scheduling
  mechanisms.
- [x] Replace plain or code-formatted public type references on those pages with Hugo `api` shortcodes and keep
  method and property names in inline code.
- [x] Keep the reaction-scheduling page limited to shared model contracts and move incarnation-specific policies to
  their respective incarnation documentation.
- [ ] Audit all website content for present-state prose. Remove statements framed around mechanisms that are absent,
  were removed, or belonged to earlier architectures; retain historical comparisons only in explicitly historical
  or migration-oriented pages.
- [ ] Audit all website references to public Alchemist types and replace bare or code-formatted class names with
  package-correct Hugo `api` shortcodes. Check that every generated target exists and that similarly named types
  resolve unambiguously.
- [ ] Cross-check the website against public KDoc/Javadoc, YAML and CLI references, examples, diagrams, and current
  implementation terminology. Resolve contradictory behavior, duplicated ownership descriptions, stale names,
  broken internal links, misleading summaries, and examples that no longer exercise the documented API.
- [ ] Keep each concept in its canonical documentation section and link to it from adjacent pages. In particular,
  keep scheduling semantics and ownership in the model scheduling page and engine-loop details in the engine page.
- [ ] Run the Hugo build and its linked API-documentation/snippet checks after the audit, then record any intentional
  historical terminology and remaining external blockers here.

## Validation protocol

Use repository Gradle tasks from the repository root.

- [x] During each phase, run the narrow compile and test tasks for touched modules.
- [x] When Kotlin changes, run `./gradlew --parallel ktlintFormat` before final verification.
- [x] When Scala in `alchemist-incarnation-scafi` changes, run
  `./gradlew --parallel alchemist-incarnation-scafi:scalafmtAll`.
- [ ] Repair Scafi formatting verification: `alchemist-incarnation-scafi:scalafmtAll` currently returns Gradle
  success while its embedded Scalafmt runner reports the configured version `3.11.5` as invalid. Until corrected,
  successful Scala compilation and the full build do not prove that the formatter actually ran.
- [x] Re-run affected module verification after formatting.
- [x] For every change, audit the existing documentation affected by it, update that documentation in the same
  change, and run the relevant documentation verification tasks when KDoc, Javadoc, website content, snippets, or
  generated documentation are impacted.
- [x] Finish every non-trivial completed change with a filtered `./gradlew --parallel build`. Exclude
  `testWithJvm*`, `jvmTestWithJvm*`, `testWithLatestJvm`, the `testWithLts*` aggregate tasks, `jpackageDeb`,
  `jpackageRpm`, `generatePKGBUILD`, `shadowJar`, and the generated `test*ShadowJarOutput` task. Keep default-JVM,
  JavaScript, WebAssembly, documentation, static-analysis, and normal integration verification enabled.
- [x] If the full build fails, record the exact blocker in this document and keep fixing until it passes or an
  external blocker is confirmed.

## Progress log

- 2026-09-01: Made the social-contagion regression direct and bounded.
- 2026-08-26: Adopted present-state, API-linked, incarnation-neutral model documentation; broader audit remains.
- 2026-08-26: Added absolute and conditional one-shot events and validity-gated reaction scheduling.
- 2026-08-25: Removed context-driven invalidation and topology callbacks from the engine.
- 2026-08-24: Unified reaction membership under `ReactionHost` and simplified loader construction.
- 2026-08-21: Renamed recurring `Event` to `GenericReaction` and replaced `Trigger` with one-shot events.
- 2026-08-20: Defined typed propensity ownership, observable-layer migration, and condition-gated Protelis sends.
- 2026-08-19: Isolated exponential residual reuse in `AbstractMarkovianNodeReaction`.
- 2026-08-18: Consolidated reaction scheduling transitions and made documentation consistency mandatory.
- 2026-08-17: Removed `BatchManager` and made `nextOccurrence` emissions update the scheduler directly.
- 2026-08-13: Made distribution cloning polymorphic and reaction clones fresh programs.
- 2026-08-03: Removed `BatchEngine` and reduced `TimeDistribution` to sampling.
- 2026-08-03: Established a compiling reactive baseline and fixed lifecycle and observable-aliasing defects.
- 2026-08-02: Merged master and migrated core collections and cognitive state to reactive APIs.
