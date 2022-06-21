/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.PhysicsDependency
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Dependency
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GlobalReaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListSet

/**
 * A global Reaction responsible for updating the physics of an [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T> @JvmOverloads constructor(
    /**
     * The environment to update.
     */
    val environment: Dynamics2DEnvironment<T>,
    updateRate: Double = 1.0,
) : GlobalReaction<T> {

    override val timeDistribution: DiracComb<T> = DiracComb(updateRate)

    override val outboundDependencies: ListSet<out Dependency> = ListSet.of(PhysicsDependency.PHYSICS)
        get() = ImmutableListSet.copyOf(field)

    override val inboundDependencies: ListSet<out Dependency> = ListSet.of()
        get() = ImmutableListSet.copyOf(field)

    override val rate: Double get() = timeDistribution.rate

    override val tau: Time get() = timeDistribution.nextOccurence

    override var actions: List<Action<T>> = listOf()

    override var conditions: List<Condition<T>> = listOf()

    override fun compareTo(other: Actionable<T>): Int = tau.compareTo(other.tau)

    override fun canExecute(): Boolean = conditions.all { it.isValid }

    override fun execute() {
        // environment.updatePhysics(0 * (1 / rate))
        timeDistribution.update(timeDistribution.nextOccurence, true, 1.0, environment)
    }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit
}
