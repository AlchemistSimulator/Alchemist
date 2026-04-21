# `alchemist-composeui` — AI Maintenance Guide

## Intent

`alchemist-composeui` provides the Compose-based user interface for Alchemist. The module exists to help users and agentic maintainers:

- inspect a running simulation visually,
- control playback with play/pause/step, jump, FPS, and pacing actions,
- inspect nodes in detail,
- toggle link visibility,
- run the same UI shell across JVM desktop and browser targets.

This document is intentionally **intent-driven**: it describes why the module exists, what users expect from it, and what invariants future changes must preserve.

## Primary jobs to be done

1. Render a live simulation viewport with nodes and optional edges.
2. Expose transport controls plus direct jump and pacing controls.
3. Show a node inspector when a node is selected.
4. Keep the UI state reactive and thread-safe.
5. Bridge the simulation engine to the Compose UI on JVM.
6. Provide a demo/fallback shell for non-simulation entrypoints.

## Current architecture

### Module structure

- `src/commonMain`: shared UI, state, and rendering logic.
- `src/jvmMain`: desktop monitor, simulation bridge, JVM-specific platform entrypoint.
- `src/wasmJsMain`: browser entrypoint and platform glue.

### Runtime entrypoints

- `App.kt` renders the shared UI shell.
- `AlchemistUiRoot.kt` composes the page layout and chooses compact vs wide layout.
- `ComposeMonitor.kt` integrates the UI with Alchemist's `OutputMonitor` on JVM.
- `DesktopAlchemistUiCallback.kt` forwards UI actions to the running simulation.
- `Main.kt` under `wasmJsMain` launches the browser demo entrypoint.

### State model

Defined in `UiModel.kt`:

- `AlchemistUiState` is the top-level UI snapshot.
- `ViewportScene` models the center canvas.
- `SimulationControlsState` models play/pause/step plus:
  - current time and step labels,
  - `To Time` and `To Step` text input values,
  - editable UI FPS,
  - event-rate slider state,
  - modal validation errors.
- `NodeInspectorState` models the selected node panel.
- `AlchemistUiCallbacks` is the interaction contract.
- `NoOpUiCallbacks` is the fallback implementation.

### State storage

Defined in `UiStore.kt`:

- `ComposeUiStateStore` wraps a `MutableStateFlow`.
- `ComposeUiController` bundles the store and callbacks.
- `demoController()` provides the browser/demo state and behavior.

### Rendering components

Shared UI is decomposed into focused composables:

- `ViewportSurface.kt`: central interactive canvas, pan/zoom, hit detection.
- `SimulationPrimaryPane.kt`: viewport + bottom control dock.
- `ControlDock.kt`: transport controls, metrics, jump inputs, FPS input, and event-rate slider.
- `NodeInspector.kt`: selected node details panel.
- `SummaryRail.kt`: summary chips and link toggle.
- `InspectorSection.kt`, `MetricBlock.kt`, `StatusPill.kt`, `TransportButton.kt`, `ProgressSection.kt`: small UI primitives.
- `Theme.kt`: colors and visual constants.
- `ViewportProjection.kt` and `ViewportRendering.kt`: coordinate transforms and drawing helpers.

### JVM data bridge

Defined in `adapter/AlchemistNodeAdapter.kt` and used by `ComposeMonitor.kt`:

- simulation nodes are projected into `ViewportNode` objects,
- environment edges are converted into `ViewportEdge`,
- simulation status is mapped into `SimulationStatus`.

## User-facing behavior

### What the user should see

- A simulation window titled `Alchemist` on desktop.
- A viewport centered on the simulation data.
- Nodes rendered with stable positions and accent-based coloring.
- Optional link rendering controlled by the UI.
- A bottom transport dock with:
  - Play,
  - Pause,
  - Step,
  - time label,
  - step counter,
  - `To Time` text box,
  - `To Step` text box,
  - `FPS` text box,
  - events/second slider ending in `Max` for full throttle.
- A node inspector when a node is selected.
- A modal popup when a jump target or numeric input is invalid.

### Interaction model

- Left-click a node to inspect it.
- Click empty space to dismiss the inspector.
- Middle-drag to pan the viewport.
- Wheel to zoom.
- Toggle links from the summary rail.
- Use play/pause/step to control the simulation when the UI is attached to a live engine.
- Press Enter in `To Time` / `To Step` to fast-forward to the requested target.
- `To Time` / `To Step` reject backward targets.
- If the simulation was already running, jump actions resume it immediately after the target is reached.
- Use `FPS` to control UI refresh frequency, clamped between `5` and the detected monitor refresh rate, or `60` if detection is unavailable.
- Use the event-rate slider to pace the simulation thread; the terminal `Max` value disables pacing and runs full throttle.

## Important invariants

1. **UI state must remain single-sourced**
   - The view layer observes `ComposeUiStateStore.stateFlow`.
   - Updates should go through the store, not through ad-hoc mutable globals.

2. **Callbacks are the only interaction boundary**
   - Composables should call `AlchemistUiCallbacks` and stay agnostic of the simulation backend.

3. **Viewport projection must remain stable enough for inspection**
   - `ViewportProjection` fixes the mapping once a valid viewport exists.
   - The selection and zoom/pan logic assume a consistent mapping between world and screen space.

4. **Live monitor updates must preserve UI-only toggles**
   - `ComposeMonitor.updateUiState` intentionally preserves `scene.showLinks` while refreshing the scene from the simulation.

5. **Validation feedback must remain store-driven**
   - Invalid jump/FPS input is surfaced via `SimulationControlsState.dialog`.
   - Composables only render and dismiss the dialog through callbacks.

6. **Demo state should remain usable without a live simulation**
   - `demoController()` is the browser-friendly fallback and should continue to demonstrate the UI shell.

7. **Small composables should stay small**
   - Maintain the current composition pattern: one responsibility per file when possible.

## Known implementation details

### `ComposeMonitor`

- Starts a desktop Compose window lazily and only once.
- Throttles UI updates according to the current FPS stored in `SimulationControlsState`.
- Detects monitor refresh rate on JVM and falls back to `60` when unavailable.
- Paces the simulation thread according to the selected events/second slider value.
- Uses `Toolkit.getDefaultToolkit().screenSize` to size the window.
- Bridges simulation state into the UI state store.

### `DesktopAlchemistUiCallback`

- Executes simulation actions on JVM.
- Synchronizes store updates on `Dispatchers.Main.immediate`.
- Uses the simulation object as the source of truth for play/pause/step/jump state.
- Validates `To Time`, `To Step`, and `FPS` submissions before mutating simulator state.
- Restores running state after a successful jump when the simulation was already running.

### `ViewportSurface`

- Manages viewport size, camera pan, and zoom.
- Uses projection data derived from the current scene.
- Keeps the first valid projection fixed until the layout becomes valid.
- Requires node selection hit tests to respect camera zoom.

### `demoController()`

- Builds a sample scene with nodes, edges, summary data, and a mock progress state.
- Supports play/pause/step and link toggling without a live simulation.
- Mirrors the running-state jump behavior used on JVM.

## Requirements for future changes

When changing this module, preserve the following:

- Keep `commonMain` free of JVM-only dependencies.
- Keep the UI reactive through `StateFlow`.
- Preserve browser entrypoint usability.
- Preserve the JVM monitor contract with `OutputMonitor`.
- Preserve node inspection, transport controls, and link toggling.
- Preserve jump validation semantics: backward `To Time` / `To Step` targets must fail with a popup.
- Preserve the constants governing FPS and event-rate ranges.
- Add or update tests when changing projection math, selection logic, or state transitions.

## Preferred change strategy

1. Identify the user intent first.
2. Locate the smallest composable or state object that owns that behavior.
3. Keep shared UI logic in `commonMain`.
4. Add platform-specific code only in the relevant source set.
5. Verify that the demo shell still works after the change.
6. Verify that the JVM monitor still attaches to the simulation without breaking state updates.

## Test and verification checklist

Before considering a change complete, check:

- `ViewportProjectionTest` still passes.
- `SimulationControlsState` behavior still matches the expected play/pause/step rules.
- Jump/FPS validation still matches the expected dialog behavior.
- Adapter tests still cover canonical edge and viewport conversion behavior.
- The UI compiles in both `commonMain` and `jvmMain`.
- The desktop monitor still opens a window and updates the UI state.
- The browser entrypoint still renders the shared `app()` shell.

## What not to change casually

- The top-level `AlchemistUiState` shape.
- The callback contract in `AlchemistUiCallbacks`.
- The preservation of `scene.showLinks` across live updates.
- The viewport math without corresponding tests.
- The separation between shared UI and platform entrypoints.

## Suggested maintainer workflow

For future modifications, prefer this order:

1. Read this file.
2. Inspect `UiModel.kt` and the relevant composable.
3. Check platform-specific behavior in `ComposeMonitor.kt` or `Main.kt` if needed.
4. Update tests near the affected logic.
5. Re-run the module validation tasks.

## Quick map of key files

- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/UiModel.kt`
- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/UiStore.kt`
- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/AlchemistUiRoot.kt`
- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/ViewportSurface.kt`
- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/ControlDock.kt`
- `src/commonMain/kotlin/it/unibo/alchemist/boundary/composeui/NodeInspector.kt`
- `src/jvmMain/kotlin/it/unibo/alchemist/boundary/composeui/ComposeMonitor.kt`
- `src/jvmMain/kotlin/it/unibo/alchemist/boundary/composeui/DesktopAlchemistUiCallback.kt`
- `src/jvmMain/kotlin/it/unibo/alchemist/boundary/composeui/adapter/AlchemistNodeAdapter.kt`
- `src/wasmJsMain/kotlin/it/unibo/alchemist/boundary/composeui/Main.kt`

## Short version

If you are an AI agent and you need to work on `alchemist-composeui`, remember:

- preserve the shared state contract,
- keep platform code separated,
- test projection and interaction math,
- do not break the demo shell,
- do not overwrite UI-only toggles when refreshing live simulation data,
- keep control validation and popup state inside the shared store model.
