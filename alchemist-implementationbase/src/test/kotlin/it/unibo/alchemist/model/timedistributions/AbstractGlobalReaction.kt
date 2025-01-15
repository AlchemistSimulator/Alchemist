/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

abstract class AbstractGlobalReaction<T, P : Position<P>>(
    private val environment: Environment<T, *>,
    distribution: TimeDistribution<T>,
) : GlobalReaction<T> {
    override var actions: List<Action<T>> = mutableListOf()
    override var conditions: List<Condition<T>> = mutableListOf()
    override val inboundDependencies: ListSet<Dependency> = ListSets.emptyListSet<Dependency>()
    override val outboundDependencies: ListSet<Dependency> = ListSets.emptyListSet<Dependency>()
    override val tau: Time = distribution.nextOccurence
    override val timeDistribution: TimeDistribution<T> = distribution
    override val rate: Double = distribution.rate

    override fun canExecute(): Boolean = true

    override fun initializationComplete(
        atTime: Time,
        environment: Environment<T, *>,
    ) { }

    override fun update(
        currentTime: Time,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>,
    ) { }

    override fun compareTo(other: Actionable<T>): Int = tau.compareTo(other.tau)

    protected abstract fun executeBeforeUpdateDistribution()

    fun nodes(): List<Node<T>> = environment.nodes

    override fun execute() {
        executeBeforeUpdateDistribution()
        timeDistribution.update(timeDistribution.nextOccurence, true, rate, environment)
    }
}
