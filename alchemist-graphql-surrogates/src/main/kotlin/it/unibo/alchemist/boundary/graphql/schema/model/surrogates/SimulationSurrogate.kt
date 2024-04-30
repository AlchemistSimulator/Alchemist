/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position

/**
 * A surrogate for [Simulation].
 * @param T the concentration type
 * @param P the position
 */
@GraphQLDescription("The simulation")
data class SimulationSurrogate<T, P : Position<out P>>(
    @GraphQLIgnore override val origin: Simulation<T, P>,
) : GraphQLSurrogate<Simulation<T, P>>(origin) {

    /**
     * The time of the simulation.
     */
    @GraphQLDescription("The status of the simulation")
    fun status(): String = origin.status.toString()

    /**
     * The time of the simulation.
     */
    @GraphQLDescription("The time of the simulation")
    fun time(): Double = origin.time.toDouble()

    /**
     * The environment of the simulation.
     */
    @GraphQLDescription("The environment of the simulation")
    fun environment(): EnvironmentSurrogate<T, P> = origin.environment.toGraphQLEnvironmentSurrogate()
}

/**
 * Converts a [Simulation] to a [SimulationSurrogate].
 * @param T the concentration type
 * @param P the position
 * @return a [SimulationSurrogate] representing the given [Simulation]
 */
fun <T, P : Position<out P>> Simulation<T, P>.toGraphQLSimulationSurrogate() = SimulationSurrogate(this)
