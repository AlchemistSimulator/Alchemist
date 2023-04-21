/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.implementations.PhysicsDependency
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListSet

/**
 * A global Reaction responsible for updating the physics of an [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T>(
    /**
     * The environment to update.
     */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T>,
) : GlobalReaction<T> {

    constructor(
        environment: Dynamics2DEnvironment<T>,
        updateRate: Double = 30.0,
    ) : this(environment, DiracComb(updateRate))

    override val outboundDependencies: ListSet<out Dependency> = ListSet.of(PhysicsDependency)
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
        environment.updatePhysics(1 / rate)
        timeDistribution.update(timeDistribution.nextOccurence, true, 1.0, environment)
    }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit
}
