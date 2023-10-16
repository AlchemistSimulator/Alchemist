/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations.mutations

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * A [SimulationHandler] provides GraphQL [Mutation] for manipulating the current simulation.
 */
class SimulationHandler<T, P : Position<out P>>(
    private val environment: Environment<T, P>,
) : Mutation {
    /**
     * Play the simulation.
     */
    @GraphQLDescription("Play the simulation")
    fun playSimulation(): String = this.environment.simulation.apply { play() }.status.toString()

    /**
     * Pause the simulation.
     */
    @GraphQLDescription("Pause the simulation")
    fun pauseSimulation(): String = this.environment.simulation.apply { pause() }.status.toString()

    /**
     * Terminate the simulation.
     */
    @GraphQLDescription("Terminate the simulation")
    fun terminateSimulation(): String = this.environment.simulation.apply { terminate() }.status.toString()
}
