# Compose UI: road to Swing parity

Working plan for PR #5554 (`compose-ui`). The goal is to make `alchemist-composeui` a full replacement for `alchemist-swingui`, running both stand-alone (desktop) and in the browser, with an effect system built from toggleable effect groups and support for map environments.

Each checkbox is a commit-sized step. Work one step at a time and commit before starting the next.

---

## Phase 0: Cleanup

Make the module consistent with the rest of the repository before building on it.

### Build

- [x] Replace the hard-coded `kotlinx-coroutines-swing:1.10.2` with a catalog entry tied to `kotlinx-coroutines` (currently 1.11.0).
- [x] Use `jvmMain { }` instead of `val jvmMain by getting`, matching master (#5465).
- [ ] Check that the `js` target from `kotlin-multiplatform-convention` and the `wasmJs` target both build. Drop either target only after a deliberate decision.

### Repository hygiene

- [x] Remove `SPEC.md` from the repository root.
- [x] Remove `alchemist-composeui/AI_CONTEXT.md`, which is out of date and lists files at old paths. Keep its still-valid invariants by moving them into KDoc or a short module README.
- [x] Remove commented-out code: the event-rate slider leftovers in `model/UiModel.kt` and `UiStore.kt`.
- [ ] Fix the 4 CodeFactor issues and the Sonar "new issues" on the PR.

### Code conventions

- [x] Remove `break`/`continue`:
  - `adapter/AlchemistNodeAdapter.kt`, `collectEdgeSnapshot`
  - `view/viewport/ViewportRendering.kt`
  - Use pipelines or `tailrec` instead.
- [x] Move shared helpers into internal plural `util` objects:
  - formatting (`formatFixed`, readable counts)
  - edge-key packing
  - geometry helpers
- [x] Split `UiStore.kt` (516 lines) into:
  - the store
  - the demo controller
  - selection/inspector reducers
- [x] Split `ViewportSurface.kt` (745 lines) into:
  - camera state
  - gesture handling (select / pan / drag)
  - keyboard handling
  - drawing
- [x] One non-private `@Composable` per file, named after it; private helpers stay with it. Split `ControlDock.kt` into `view/controls/`.
- [x] Reorder members according to `AGENTS.md`, in a separate `style` commit.
- [x] KDoc on every public declaration. Reduce visibility to `internal` wherever possible; the public API should be `ComposeMonitor` plus the effect/extension SPI.

### Behaviour fixes

- [x] Enter in the viewport selects "the first node". Remove it, or replace it with a meaningful action.
- [x] Nodes are all drawn with the same default accent. Until effects land, keep a neutral default.
- [x] `ComposeMonitor` on a headless JVM must not crash. Add `failOnHeadless` (default `false`) as in `SwingGUI`: log a warning and do nothing.
- [x] Closing the window calls `exitApplication()` only, so the simulation keeps running. Decide the close semantics, matching Swing's `EXIT_ON_CLOSE` default: terminate the simulation, then exit.
- [x] Check that the terminated/finished state is displayed when the simulation ends.
- [x] `throttleSimulation` sleeps on the simulation thread on every step. Keep it, but make sure pausing or terminating the simulation never waits on that sleep.

### Look and controls

- [ ] Show the Alchemist icon next to "Alchemist Simulator" at the top left. Source: `site/static/images/logo.svg`; ship it as a resource of the module.
- [ ] Use the same icon as the application/window icon.
- [ ] "To Step" defaults to the current step + 1; "To Time" defaults to the current time + 1. Keep them in sync while the simulation runs, unless the user is editing the field.
- [ ] All controls in the bottom bar have the same size.
- [ ] Scale the whole UI (bottom bar included) with the window size, so it looks right at any screen size and density. Sizes come from the window, not from fixed `dp` values tuned on one screen.
- [ ] Make the inspector (right side bar) resizable by dragging its edge.
- [ ] Let the inspector edit the contents of the selected nodes: change, add and remove molecules and their concentrations, for one node or for the whole selection. Values are parsed by the incarnation, and edits run on the simulation thread.

### Pacing

- [ ] Replace the event-rate slider with explicit pacing modes:
  - events per second;
  - simulated time units per second (real-time with a speed multiplier; covers Swing's `R`);
  - maximum speed.
  A mode selector plus a numeric field is likely clearer than a slider.
- [ ] Unlink the UI frame rate from the simulation pacing. The FPS cap (default 30, maximum 240) only limits how often the UI redraws, so the simulation can run fast without the UI eating CPU. It must never slow down or speed up the simulation.

### Tests

- [ ] Build JVM test scenarios through the Kotlin script DSL / generated factories instead of hand-made stubs:
  - `DesktopAlchemistUiCallbackTest`
  - `DesktopAlchemistUiNodeMoveTest`
- [ ] Use backtick test names and `kotlin.test` throughout; no timing-based assertions.

---

## Phase 1: Visual feedback loop

A way to look at the UI while developing, for both the developer and the agent.

- [ ] **Snapshot renderer (JVM, headless).** A Gradle task, e.g. `alchemist-composeui:renderSnapshots`, that:
  - loads a set of scenario files from `src/jvmTest/resources/snapshots/*.yml` (or `.alchemist.kts`);
  - runs each for a fixed number of steps with a fixed seed;
  - renders the full UI offscreen (`ImageComposeScene`) at a few sizes: wide, compact, and an inspector/effects panel open;
  - writes PNGs to `alchemist-composeui/build/ui-snapshots/<scenario>-<size>.png`.

  These are review images, not assertions, so they cannot become flaky tests. The agent can open the PNGs directly; the user can open the folder.
- [ ] **Scripted UI states.** Each snapshot can declare UI actions to apply before rendering: select nodes, toggle groups, set zoom/pan, open panels. This lets one image show one specific feature.
- [ ] **Live desktop run.** A task, e.g. `alchemist-composeui:runDemo`, that launches the desktop UI on a chosen scenario (`-Pscenario=...`) for hands-on inspection.
- [ ] **Live browser run.** `wasmJsBrowserDevelopmentRun` on the demo controller now; on a real simulation once Phase 4 lands.
- [ ] **Browser snapshots (optional, later).** Headless Chromium screenshots of the wasm build, so both targets can be compared visually.
- [ ] **Scenarios.** Add `snapshots/` scenarios covering:
  - a plain 2D grid
  - a dense random deployment with links
  - obstacles
  - a map environment
  - a layer
  - navigation graph / cognitive agents
  - smartcam

  Phases 5–7 grow this set as their features land.

---

## Phase 2: Core architecture (prerequisite for effects and the browser)

Separate what the simulation produces from how it is shown, so the same UI can run in-process or remotely.

- [ ] **Frame model** in `commonMain`, `@Serializable`: a `SceneFrame` holding
  - time, step, status (mirrored as `SimulationStatus` until Phase 8);
  - nodes: id, position, and only the values requested by active effects;
  - links;
  - environment-level primitives: polygons, polylines, raster grids, markers;
  - the coordinate system (Euclidean or geographic).

  It replaces the current `ViewportScene` payload; `ViewportScene` keeps only UI state.
- [ ] **Session abstraction** in `commonMain`: a `SimulationSession` with a `frames: StateFlow<SceneFrame>`, commands (play, pause, step, go to time/step, move/clone/delete nodes, inject molecule, set throttling/real-time), and the active effect configuration.
  - `LocalSession` (JVM) wraps the current `DesktopAlchemistUiCallback` logic.
  - `RemoteSession` (common: JVM, JS, wasm) is implemented in Phase 4.
- [ ] **Frame builder** (JVM): turns `Environment` into a `SceneFrame`. It extracts only what the current effect set requests (Phase 3), throttled to the UI FPS. It runs on the simulation thread, and the result is published immutably.
- [ ] **Projection abstraction**: `WorldProjection` with an identity/Euclidean implementation now and Web Mercator in Phase 5. Camera, hit-testing, selection and drag must work through it.
- [ ] Move the demo controller onto a `FakeSession` so the browser demo exercises the same code paths.

---

## Phase 3: Effect system with effect groups

### Model (`commonMain`, `@Serializable`)

- [ ] `Effect`: `id`, `name`, `enabled`, plus parameters.
  - **Node effects** are applied to each visible node.
  - **Scene effects** are drawn once per frame: obstacles, layers, graphs.
- [ ] `EffectGroup`: `name`, `enabled`, `opacity`, and an ordered list of effects. The UI shows an ordered list of groups. Toggling a group shows or hides a whole visualisation in one click; toggling an effect within a group fine-tunes it.
- [ ] **Data requirements.** Each effect declares what it needs, for example "concentration of molecule `x`", "property `p` of molecule `x`" (DrawShape semantics through `Incarnation.getProperty`), "obstacles", "layer `L` sampled at N×N", "navigation graph". The frame builder collects only the union of the requirements of enabled effects. Disabled groups cost nothing.
- [ ] **Rendering.** A common renderer draws effects through `DrawScope`, in group order and effect order within a group, applying group opacity.
- [ ] **Parameter descriptors.** Each effect type exposes typed parameter descriptors: number with range, colour, enum, molecule name, boolean. The editor UI is generated from these, with no reflection.
- [ ] **Extensibility.** A polymorphic serializer registry, with JVM extractor contributions discovered through `ServiceLoader`, so new effects can be added without touching the core.

### Configuration

- [ ] JSON format (kotlinx.serialization) usable on all targets.
- [ ] `ComposeMonitor` parameter `effects: <path or classpath resource>`, mirroring `SwingGUI`'s `graphics`.
- [ ] Inline definition in the simulation YAML / Kotlin DSL under the monitor's parameters.
- [ ] Default configuration when none is given: one group, "Nodes", with a plain node effect, plus a "Links" group that is off by default.
- [ ] Once links are an effect, remove the ad-hoc link state from the UI model: `LinkRenderMode`, `ViewportScene.showLinks`, `linkRenderMode`, `linkRenderNotice`, and the summary-bar links toggle.
- [ ] Legacy import (JVM): read Swing effect JSON (`DrawShape` and friends) and convert it to the new format, so existing `effects/*.json` (e.g. in `external-resources/learning-scafi-alchemist`) keep working.

### UI

- [ ] Effects panel (side drawer; bottom sheet on compact layouts):
  - group list with on/off toggle, opacity, rename, reorder (drag or up/down), add and remove;
  - effects inside a group, with the same operations.
- [ ] Effect editor generated from the parameter descriptors. Edits apply live.
- [ ] Molecule pickers populated from molecules observed in the simulation.
- [ ] Save/load the configuration:
  - desktop: file dialog;
  - browser: download/upload.
- [ ] Keyboard toggles for the first 9 groups (`1`–`9`).
- [ ] Legend: per group, a colour summary of each effect (Swing's colour chips, but readable).

---

## Phase 4: Stand-alone and browser

- [ ] **Server** (JVM, Ktor): `ComposeMonitor` gains a mode parameter: `desktop` (default), `browser`, or `both`.
  - In browser mode it serves the wasm bundle and a WebSocket endpoint that streams `SceneFrame`s and receives commands.
  - It works on headless machines; print the URL at startup.
- [ ] **Bundle packaging.** Copy the `wasmJsBrowserDistribution` output into the JVM jar's resources at build time, so `alchemist run sim.yml` with `mode: browser` needs nothing else.
- [ ] **`RemoteSession`** (common Ktor client WebSocket):
  - reconnect handling;
  - frames delta-encoded or throttled to the client's requested FPS;
  - effect configuration changes sent to the server, so it extracts the right data.
- [ ] **Multiple clients.** Several browsers can watch the same simulation. Control commands are shared; effect configuration is per client.
- [ ] **Transport choice.** A dedicated WebSocket protocol on the shared `@Serializable` frame model is recommended over extending `alchemist-graphql`: effects need server-side extraction of arbitrary, effect-specific data, which does not fit a generic node/molecule schema. Revisit if GraphQL must stay the public remote API.
- [ ] Snapshot/test coverage for the browser: protocol round-trip tests (frames and commands) in `commonTest` and `jvmTest`.

---

## Phase 5: Maps

- [ ] **Coordinate system.** Detect `GeoPosition` environments (`MapEnvironment`). Frames carry lat/lon, and the viewport uses Web Mercator through `WorldProjection`.
- [ ] **Tile layer** (common, so it works on desktop and in the browser):
  - slippy-map tiles fetched with the Ktor client;
  - decoded to `ImageBitmap`;
  - in-memory LRU cache on all targets, plus a disk cache on the JVM, matching Swing's 256/2048 split;
  - integer zoom levels with scaled tiles in between for smooth zoom;
  - parent-tile fallback while tiles load.
- [ ] **Tile source configuration:** URL template and attribution, OSM by default. Follow the OSM tile usage policy: a meaningful User-Agent, as in `InitMapsForge.kt`, and a visible "© OpenStreetMap contributors" attribution.
- [ ] **Scale bar.**
- [ ] **Browser:** tiles are fetched by the client directly, or proxied by the server when CORS or offline use requires it.
- [ ] **Map as an effect group.** The base map is the bottom group, "Map", toggleable like any other.
- [ ] **Snapshot scenario** with a small map environment from `alchemist-maps` test resources. Snapshots use a local tile fixture to avoid network access.

---

## Phase 6: Domain effects (Swing parity)

One step per effect: extractor (JVM), renderer (common), descriptor, snapshot scenario.

- [ ] **Node shape** (`DrawShape` parity):
  - ellipse/rectangle, filled or outlined;
  - RGBA, size, aspect ratio;
  - "only nodes containing molecule";
  - colour from a molecule property on a chosen channel (A/R/G/B/H/S/B), with min/max/order of magnitude and reverse;
  - "write the value".
- [ ] **Links:** colour and width; current sampling policy for dense graphs.
- [ ] **Obstacles** (`Environment2DWithObstacles`): actual shapes rather than Swing's bounding boxes; refreshed only when obstacles move.
- [ ] **Navigation graph** (`ImageEnvironmentWithGraph`): rooms, passages, centroid links.
- [ ] **Cognitive map** of the selected node (`OrientingProperty`).
- [ ] **Trails** (`DrawPedestrianPath` / trajectory part of `DrawDirectedNode`): per-node trail with length and sampling frequency; fading.
- [ ] **Directed node:** heading from a molecule or from physics, hue from a molecule.
- [ ] **Smartcam:** physical shape with heading, and camera field-of-view arc.
- [ ] **Layers:** gradient (sampled N×N) and isolines (move the CONREC finder into a shared module or port it), with linear/log distribution and value labels. Generalise beyond `BidimensionalGaussianLayer` where layers expose sampling.
- [ ] **Image background** for `ImageEnvironment`, if used by scenarios.

---

## Phase 7: Documentation and transition

- [ ] Reference page for the Compose UI, alongside `site/content/reference/swing/`: launch options, modes, shortcuts, effects and groups, maps.
- [ ] Write the "swing-effects" how-to that is currently a stub, as a Compose effects how-to, including conversion of legacy effect files.
- [ ] Switch site examples and `src/test/resources/*.alchemist.kts` samples to `ComposeMonitor` where appropriate.
- [ ] Decide when `SwingGUI` is removed. It is already `@Deprecated`.

---

## Phase 8: Multiplatform simulation core

Use the simulation primitives directly in the UI instead of mirroring them: `SimulationState` instead of `SimulationStatus`, and then `Time`, positions, and the other types that `SceneFrame` copies.
This requires `alchemist-api` and `alchemist-engine` to be Kotlin Multiplatform (pure Kotlin, no JVM-only API in `commonMain`).
It is a change to the core of the simulator, independent of the Compose work, and can be developed in parallel.

- [ ] Convert `alchemist-api` to Kotlin Multiplatform. Today it has no Java sources, but it uses JVM-only API:
  - `java.util` and `java.util.concurrent` (`CompletableFuture` in `Simulation`), to be replaced with common types (e.g. coroutines `Deferred`, or a minimal common future);
  - `apache-commons-math3` and `jool` as `api` dependencies, to be moved out of the public API or replaced;
  - `kotlin-reflect` and `kotlinx-collections-immutable-jvm` (switch to the multiplatform artifact).
- [ ] Convert `alchemist-engine` to Kotlin Multiplatform. Its JVM-only parts are the threading model in `AbstractEngine` (locks, `BlockingQueue`, `Thread`, `@Synchronized`), `slf4j`, `guava`, `trove4j`, and `jgrapht`. The thread-based runner can stay in `jvmMain`, behind a common engine core.
- [ ] Make `alchemist-observables` multiplatform (it is JVM-only today). `alchemist-maintenance-tooling` and `arrow-core` already are.
- [ ] Depend on `alchemist-api` from `alchemist-composeui` `commonMain`, then remove `SimulationStatus` and the other mirrored types, serialising the core types directly in `SceneFrame`.

---

## Open decisions

- Transport for browser mode: dedicated WebSocket (recommended) or GraphQL.
- Keep both `js` and `wasmJs` targets, or wasm only.
- Effect configuration: JSON only, or also YAML on the JVM.
- Where domain-effect extractors live: in `alchemist-composeui` `jvmMain` (simplest, as Swing does) or contributed through `ServiceLoader` by domain modules (cleaner dependencies).
