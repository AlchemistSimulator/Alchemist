/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.model.Position

/**
 * Sets the speed limit, which the [throttle] reads at every step.
 * Each change wakes the [throttle] up, so a long wait at the old pace ends at once.
 */
internal class EnginePace<T, P : Position<P>>(
    private val bridge: EngineBridge<T, P>,
    private val throttle: SimulationThrottle,
) : AlchemistUiCallbacks.Pace {
    override suspend fun setMode(mode: PacingMode) {
        bridge.updateState { it.copy(controls = it.controls.withPacingMode(mode)) }
        throttle.wakeUp()
    }

    override suspend fun setRateInput(value: String) {
        bridge.updateState { it.copy(controls = it.controls.withPacingInput(value)) }
        throttle.wakeUp()
    }
}
