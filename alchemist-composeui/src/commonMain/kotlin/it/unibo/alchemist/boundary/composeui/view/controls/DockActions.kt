/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

/**
 * The callbacks triggered by the controls of the dock.
 */
internal class DockActions(
    val onPlay: () -> Unit,
    val onPause: () -> Unit,
    val onStep: () -> Unit,
    val onToTimeInputChanged: (String) -> Unit,
    val onToTimeSubmit: () -> Unit,
    val onToStepInputChanged: (String) -> Unit,
    val onToStepSubmit: () -> Unit,
    val onFpsInputChanged: (String) -> Unit,
    val onFpsSubmit: () -> Unit,
    val onEventRateChanged: (Float) -> Unit,
)
