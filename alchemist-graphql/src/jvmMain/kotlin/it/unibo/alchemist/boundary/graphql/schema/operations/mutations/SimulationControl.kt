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
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * A [SimulationControl] provides GraphQL [Mutation] for manipulating the current simulation.
 */
class SimulationControl<T, P : Position<out P>>(
    private val environment: Environment<T, P>,
) : Mutation {

    /**
     * Play the simulation.
     */
    @GraphQLDescription("Play the simulation")
    fun play(): String = executeAction(Simulation<T, P>::play, Status.RUNNING)

    /**
     * Pause the simulation.
     */
    @GraphQLDescription("Pause the simulation")
    fun pause(): String = executeAction(Simulation<T, P>::pause, Status.PAUSED)

    /**
     * Terminate the simulation.
     */
    @GraphQLDescription("Terminate the simulation")
    fun terminate(): String = executeAction(Simulation<T, P>::terminate, Status.TERMINATED)

    private fun executeAction(action: Simulation<T, P>.() -> Unit, status: Status) =
        runCatching { this.environment.simulation.apply { action() } }
            .fold(
                onSuccess = { status.toString() },
                onFailure = { it.message.toString() },
            )
}
