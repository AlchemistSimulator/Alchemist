/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate

/**
 * Context for configuring termination predicates in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
@AlchemistDsl
interface TerminatorsContext<T, P : Position<P>> {
    /** The parent simulation context. */
    val ctx: SimulationContext<T, P>

    /**
     * Adds a termination predicate to the simulation.
     *
     * @param this The termination predicate to add.
     */
    operator fun TerminationPredicate<*, *>.unaryPlus()
}

/**
 * Implementation of [TerminatorsContext].
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
class TerminatorsContextImpl<T, P : Position<P>>(override val ctx: SimulationContext<T, P>) :
    TerminatorsContext<T, P> {
    @Suppress("UNCHECKED_CAST")
    override fun TerminationPredicate<*, *>.unaryPlus() {
        ctx.environment.addTerminator(this as TerminationPredicate<T, P>)
    }
}
