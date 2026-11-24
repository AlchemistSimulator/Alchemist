/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.callbacks

import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode

/**
 * Callbacks that do nothing, for previews and tests.
 */
internal object NoOpUiCallbacks : AlchemistUiCallbacks {
    override val transport: AlchemistUiCallbacks.Transport = object : AlchemistUiCallbacks.Transport {
        override suspend fun play() = Unit

        override suspend fun pause() = Unit

        override suspend fun step() = Unit

        override suspend fun runToTime(target: Double) = Unit

        override suspend fun runToStep(target: Long) = Unit

        override suspend fun jumpTimeBy(delta: Double) = Unit

        override suspend fun jumpStepBy(delta: Long) = Unit
    }

    override val pace: AlchemistUiCallbacks.Pace = object : AlchemistUiCallbacks.Pace {
        override suspend fun setMode(mode: PacingMode) = Unit

        override suspend fun setRateInput(value: String) = Unit
    }

    override val nodes: AlchemistUiCallbacks.Nodes = object : AlchemistUiCallbacks.Nodes {
        override suspend fun move(nodePositions: List<NodePositionUpdate>) = Unit

        override suspend fun clone(copies: List<NodePositionUpdate>) = Unit

        override suspend fun delete(nodeIds: List<Int>) = Unit

        override suspend fun setConcentration(nodeIds: List<Int>, molecule: String, value: String) =
            Result.success(Unit)

        override suspend fun removeMolecule(nodeIds: List<Int>, molecule: String) = Unit
    }

    override val display: AlchemistUiCallbacks.Display = object : AlchemistUiCallbacks.Display {
        override suspend fun inspect(nodeIds: List<Int>) = Unit

        override suspend fun toggleLinks() = Unit
    }
}
