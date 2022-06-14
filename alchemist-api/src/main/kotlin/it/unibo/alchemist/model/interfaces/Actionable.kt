/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import org.danilopianini.util.ListSet
import java.io.Serializable

/**
 * A time-distributed entity with [inboundDependencies], [outboundDependencies] and an execution strategy.
 */
sealed interface Actionable<T> : Comparable<Actionable<T>>, Serializable {

    /**
     * @return true if the reaction can be executed (namely, all the conditions
     * are satisfied).
     */
    fun canExecute(): Boolean

    /**
     * Executes the reactions.
     */
    fun execute()

    /**
     * This method is called when the environment has completed its
     * initialization. Can be used by this reaction to compute its next
     * execution time - in case such computation requires an inspection of the
     * environment.
     *
     * @param atTime
     * the time at which the initialization of this reaction was
     * accomplished
     * @param environment
     * the environment
     */
    fun initializationComplete(atTime: Time, environment: Environment<T, *>)

    /**
     *  The list of [Action]s of the [Reaction].
     *  Please be careful when you modify this list.
     */
    var actions: List<Action<T>>

    /**
     * The list of [Condition]s of the [Reaction].
     * Please be careful when you modify this list.
     */
    var conditions: List<Condition<T>>

    /**
     * @return The list of [Dependency] whose concentration may change after the
     * execution of this reaction.
     */
    val outboundDependencies: ListSet<out Dependency>

    /**
     * @return The list of [Dependency]s whose concentration may affect the
     * execution of the [Reaction].
     */
    val inboundDependencies: ListSet<out Dependency>

    /**
     * Returns the speed of this [Reaction]. It is an average number, and
     * can potentially change during the simulation, depending on the
     * implementation.
     *
     * @return the number of times this [Reaction] is triggered per time
     * unit.
     */
    val rate: Double get() = timeDistribution.rate

    /**
     * @return The global [Time] at which this reaction is scheduled to be
     * executed
     */
    val tau: Time get() = timeDistribution.nextOccurence

    /**
     * @return the [TimeDistribution] for this [Reaction]
     */
    val timeDistribution: TimeDistribution<T>

    /**
     * Updates the scheduling of this reaction.
     *
     * @param hasBeenExecuted
     * true if the reaction have just been executed.
     * @param currentTime
     * the current [Time] of execution. This is mandatory in
     * order to correctly compute the time shift of an
     * already-scheduled reaction
     * @param environment
     * the current environment
     */
    fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>)
}
