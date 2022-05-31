/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Dependency
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GlobalReaction
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

class PhysicsUpdate<T>(
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T>,
) : GlobalReaction<T> {

    override val outboundDependencies: ListSet<out Dependency> get() = ListSets.emptyListSet()

    override val inboundDependencies: ListSet<out Dependency> get() = ListSets.emptyListSet()

    override val inputContext: Context get() = Context.GLOBAL

    override val outputContext: Context = Context.GLOBAL

    override val rate: Double get() = timeDistribution.rate

    override val tau: Time get() = timeDistribution.nextOccurence

    override var actions: List<Action<T>> = listOf()
        private set

    override var conditions: List<Condition<T>> = listOf()
        private set

    override fun compareTo(other: Reaction<T>): Int = tau.compareTo(other.tau)

    override fun canExecute(): Boolean = conditions.all { it.isValid }

    override fun execute() {
        environment.updatePhysics(1 / rate)
    }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun setConditions(conditions: List<Condition<T>>) {
        this.conditions = conditions
    }

    override fun setActions(actions: List<Action<T>>) {
        this.actions = actions
    }

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit
}
