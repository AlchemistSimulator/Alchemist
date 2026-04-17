# Alchemist Compose UI

## Goals
- Provide a modern Compose-based simulator UI centered on the rendered environment.
- Keep the visual structure common across targets, with platform modules only adapting simulator state and commands.
- Establish a living specification that can grow with future iterations without forcing a redesign of the document.

## Design Principles
- Canvas first: the simulation viewport is the dominant surface and receives the strongest visual emphasis.
- Common by default: layout, presentation logic, theming, and interaction shell live in `commonMain`.
- Thin platform adapters: JVM, JS, and WASM modules only translate simulator state into common UI models and dispatch user actions back to the host runtime.
- Progressive disclosure: node details stay out of the main viewport until a selection occurs.
- Low-chrome modernity: restrained translucent panels, clear hierarchy, and motion only where it explains state change.

## Information Architecture
### Primary regions
- Central viewport: renders nodes and, when available, map-oriented backdrops.
- Bottom control dock: hosts transport controls, status, time, step, and progress.
- Contextual inspector: reveals information for the selected node.

### Secondary overlays
- Viewport summary chips: node count, dimensions, and scene backdrop.
- Empty/loading/error copy: rendered inside the viewport shell without changing the platform adapter contract.
- Compact inspector shell: becomes a drawer or bottom sheet on narrow layouts.

## Layouts
### Wide layout
- Viewport fills the remaining space.
- Bottom control dock is anchored to the bottom center.
- Inspector is anchored to the right edge and remains visible while a node is selected.

### Compact layout
- Viewport stays full-width above the bottom dock.
- Inspector becomes an overlay sheet above the controls.
- The scrim dismisses the inspector when the user clicks outside it.

## Components
### Viewport
- Compose canvas with a shared visual shell and background treatment.
- Supports node highlighting, node hit-testing, drag-to-pan, mouse-wheel zoom, and summary overlays.
- Accepts normalized viewport nodes from the platform adapter rather than raw simulator entities.

### Control dock
- Play, pause, and step actions.
- Status pill with color-coded simulator state.
- Time and step metric blocks.
- Progress bar with determinate mode when completion is known, indeterminate otherwise.

### Node inspector
- Header with node identifier and dismiss action.
- Position section.
- Concentrations section.
- Metadata section for simulator-provided details already available or straightforward to expose.

## State Model
- `AlchemistUiState`: top-level UI state for the screen.
- `ViewportScene`: viewport payload including nodes, dimensions, backdrop, summary chips, and viewport copy.
- `SimulationControlsState`: simulator status, time label, step count, and progress descriptor.
- `NodeInspectorState`: visible node details for the selected node.
- `ComposeUiStateStore`: thread-safe holder updated by platform monitors/adapters.

## Platform Integration Boundaries
- `commonMain` owns:
  - theming
  - layout
  - viewport rendering shell
  - node selection interactions
  - control dock presentation
  - inspector presentation
- `jvmMain`, `jsMain`, and `wasmJsMain` own:
  - lifecycle bootstrapping
  - mapping simulator/runtime state into common UI models
  - dispatching user actions such as play, pause, and step
  - platform-specific transport or monitor glue

## Interaction Patterns
- Clicking a node opens the inspector for that node.
- Clicking outside a selected node dismisses the inspector.
- Holding the middle mouse button and dragging pans the camera.
- Using the mouse wheel zooms the viewport in and out around the pointer position.
- Panning is unbounded in every direction, with no camera clamp (CAD-like navigation).
- Control buttons reflect simulator availability:
  - `Play` is enabled in ready/paused states.
  - `Pause` is enabled while running.
  - `Step` is enabled in ready/paused states.
- The step action is part of the UI contract even if some targets still need dedicated wiring.

## Visual Tokens
- Palette: deep blue/slate surfaces with warm amber and cool cyan accents.
- Shapes: large rounded panels for the dock, viewport shell, and inspector.
- Typography:
  - serif headings for emphasis
  - monospace metrics for time, steps, and machine-like values
- Motion:
  - inspector slide/fade transitions
  - no decorative motion in the viewport beyond state-relevant highlighting

## Accessibility
- Controls use text labels instead of icon-only affordances.
- Status is encoded with both text and color.
- Important values remain visible in compact mode.
- Inspector dismissal is possible both from an explicit button and by clicking the compact-mode scrim.

## Open Questions / Future Iterations
- Dedicated map tile or raster/vector map underlay support.
- Fit-to-scene and reset-camera shortcuts.
- Additional inspector sections for reactions, neighborhood members, and domain-specific node properties.
- Multi-selection and aggregate inspector views.
- Richer progress semantics for simulations that expose completion estimates.
