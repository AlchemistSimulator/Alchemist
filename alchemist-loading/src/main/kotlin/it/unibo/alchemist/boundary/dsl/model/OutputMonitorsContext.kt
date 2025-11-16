/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Position

/**
 * Context for configuring output monitors in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
interface OutputMonitorsContext<T, P : Position<P>> {
    /** The parent simulation context. */
    val ctx: SimulationContext<T, P>

    /**
     * Adds an output monitor to the simulation.
     *
     * @param this The output monitor to add.
     */
    operator fun OutputMonitor<T, P>.unaryPlus()
}

/**
 * Implementation of [OutputMonitorsContext].
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
class OutputMonitorsContextImpl<T, P : Position<P>>(override val ctx: SimulationContextImpl<T, P>) :
    OutputMonitorsContext<T, P> {
    override fun OutputMonitor<T, P>.unaryPlus() {
        ctx.monitors += this
    }
}
