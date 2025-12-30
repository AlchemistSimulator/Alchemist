/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Position

/**
 * Context for configuring global reactions in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
interface GlobalProgramsContext<T, P : Position<P>> {
    /** The parent simulation context. */
    val ctx: SimulationContext<T, P>

    /**
     * Adds a global reaction to the simulation.
     *
     * @param this The global reaction to add.
     */
    operator fun GlobalReaction<T>.unaryPlus()
}

/**
 * Implementation of [GlobalProgramsContext].
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
class GlobalProgramsContextImpl<T, P : Position<P>>(override val ctx: SimulationContext<T, P>) :
    GlobalProgramsContext<T, P> {
    override fun GlobalReaction<T>.unaryPlus() {
        ctx.environment.addGlobalReaction(this)
    }
}
