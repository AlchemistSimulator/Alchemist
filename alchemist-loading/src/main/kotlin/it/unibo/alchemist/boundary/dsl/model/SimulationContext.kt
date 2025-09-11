/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.DslLoader
import it.unibo.alchemist.boundary.dsl.model.Incarnation as Inc
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.jvm.optionals.getOrElse

class SimulationContext<T, P : Position<P>>(val incarnation: Incarnation<T, P>, val environment: Environment<T, P>) {
    var monitors: List<OutputMonitor<T, P>> = emptyList()
    private var _networkModel: LinkingRule<T, P> = NoLinks()
    var networkModel: LinkingRule<T, P>
        get() = _networkModel
        set(value) {
            _networkModel = value
            environment.linkingRule = value
        }

    fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        DeploymentsContext(this).apply(block)
    }
    fun addTerminator(predicate: TerminationPredicate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        environment.addTerminator(predicate as TerminationPredicate<T, P>)
    }
    fun addMonitor(monitor: OutputMonitor<T, P>) {
        monitors = monitors + (monitor)
    }
}

fun <T, P : Position<P>> createLoader(simBuilder: SimulationContext<T, P>): Loader = object : DslLoader(simBuilder) {
    override val constants: Map<String, Any?> = emptyMap()
    override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
    override val variables: Map<String, Variable<*>> = emptyMap()
    override val remoteDependencies: List<String> = emptyList()
    override val launcher: Launcher = DefaultLauncher()
}

fun <T, P : Position<P>> Inc.incarnation(): Incarnation<T, P> = SupportedIncarnations.get<T, P>(this.name).getOrElse {
    throw IllegalArgumentException("Incarnation $this not supported")
}

fun <T, P : Position<P>> simulation(
    incarnation: Incarnation<T, P>,
    environment: Environment<T, P>,
    block: SimulationContext<T, P>.() -> Unit,
): Loader {
    val sim = SimulationContext(incarnation, environment).apply(block)
    return createLoader(sim)
}

fun <T, P : Position<P>> simulation(
    incarnation: Incarnation<T, P>,
    block: SimulationContext<T, Euclidean2DPosition>.() -> Unit,
): Loader {
    @Suppress("UNCHECKED_CAST")
    val defaultEnv = Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>)
    val sim = SimulationContext(incarnation, defaultEnv).apply(block)
    return createLoader(sim)
}
