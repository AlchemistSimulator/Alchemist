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
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.DslLoader
import it.unibo.alchemist.boundary.dsl.model.Incarnation as Inc
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.jvm.optionals.getOrElse

class SimulationContext<T> {
    var incarnation: Incarnation<T, *>? = null
    var environment: Environment<T, *>? = null

    @Suppress("UNCHECKED_CAST")
    val default: Environment<T, Euclidean2DPosition> by lazy {
        require(incarnation != null) {
            "Incarnation must be set before accessing default environment"
        }
        Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>)
    }

    fun <C : Position<C>> environment(env: Environment<T, C>, block: EnvironmentContext<T, C>.() -> Unit) {
        this.environment = env
        EnvironmentContext(this, env).apply(block)
    }
    fun environment(block: EnvironmentContext<T, Euclidean2DPosition>.() -> Unit) {
        val env = default
        this.environment = default
        EnvironmentContext(this, env).apply(block)
    }
}

class EnvironmentContext<T, P : Position<P>>(val ctx: SimulationContext<T>, val environment: Environment<T, P>) {

    private var _networkModel: LinkingRule<T, P> = NoLinks()
    var networkModel: LinkingRule<T, P>
        get() = _networkModel
        set(value) {
            _networkModel = value
            environment.linkingRule = value
        }

    @Suppress("UNCHECKED_CAST")
    val incarnation: Incarnation<T, P>
        get() = ctx.incarnation as? Incarnation<T, P>
            ?: error("Incarnation not defined or of the wrong type")

    fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        DeploymentsContext(this).apply(block)
    }
}

fun <T, P : Position<P>> createLoader(simBuilder: SimulationContext<T>): Loader = object : DslLoader(simBuilder) {
    override val constants: Map<String, Any?> = emptyMap()
    override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
    override val variables: Map<String, Variable<*>> = emptyMap()
    override val remoteDependencies: List<String> = emptyList()
    override val launcher: Launcher = DefaultLauncher()
}

fun <T, P : Position<P>> Inc.incarnation(): Incarnation<T, P> = SupportedIncarnations.get<T, P>(this.name).getOrElse {
    throw IllegalArgumentException("Incarnation $this not supported")
}

fun <T, P : Position<P>> simulation(incarnation: Incarnation<T, P>, block: SimulationContext<T>.() -> Unit): Loader {
    val sim = SimulationContext<T>().apply {
        this.incarnation = incarnation
    }.apply(block)
    return createLoader(sim)
}

fun simulation(block: SimulationContext<Any>.() -> Unit): Loader {
    val sim = SimulationContext<Any>().apply(block)
    return createLoader(sim)
}
