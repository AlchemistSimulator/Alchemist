/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import org.apache.commons.math3.random.MersenneTwister

abstract class DslLoader(private val ctx: SimulationContext) : Loader {
    @Suppress("UNCHECKED_CAST")
    override fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P> {
        val incarnation = SupportedIncarnations.get<T, P>(ctx.incarnation.name).get()
        val randomGenerator = MersenneTwister(0)
        val environment = ctx.envCtx?.environment as Environment<T, P>
        // Get deployments and add nodes
        val deployments = ctx.envCtx?.ctxDeploy?.deployments ?: emptyList()
        deployments.forEach { deployment ->
            deployment.forEach { position ->
                val node = incarnation.createNode(
                    randomGenerator,
                    environment,
                    null,
                )
                environment.addNode(node, position as P)
            }
        }

        return Engine(environment)
    }
}
