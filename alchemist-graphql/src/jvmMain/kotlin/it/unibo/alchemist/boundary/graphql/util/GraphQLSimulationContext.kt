/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.util

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position

/**
 * A context for a [Simulation] used by GraphQL Server.
 * @param simulation the simulation
 * @param environmentEmitter the environment emitter as a [EnvironmentEventBus]
 */
data class GraphQLSimulationContext<T, P : Position<out P>>(
    val simulation: Simulation<T, P>,
    val environmentEmitter: EnvironmentEventBus<T, P> = EnvironmentEventBus(simulation),
)
