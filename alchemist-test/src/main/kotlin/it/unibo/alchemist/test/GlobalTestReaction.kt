/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

class GlobalTestReaction<T>(
    override val timeDistribution: TimeDistribution<T>,
    val environment: Environment<T, *>,
) : GlobalReaction<T> {

    override fun compareTo(other: Actionable<T>): Int = tau.compareTo(other.tau)

    override fun canExecute(): Boolean = conditions.all { it.isValid }

    override fun execute() = timeDistribution.update(timeDistribution.nextOccurence, true, 1.0, environment)

    override var actions: List<Action<T>> = emptyList()

    override var conditions: List<Condition<T>> = emptyList()

    override val outboundDependencies: ListSet<out Dependency> = ListSets.emptyListSet()

    override val inboundDependencies: ListSet<out Dependency> = ListSets.emptyListSet()

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit
}
