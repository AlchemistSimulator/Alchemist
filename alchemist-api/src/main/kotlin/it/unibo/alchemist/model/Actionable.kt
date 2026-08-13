/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable
import java.io.Serializable

/**
 * A time-distributed entity with an execution strategy.
 */
sealed interface Actionable<T> :
    Comparable<Actionable<T>>,
    Serializable,
    Disposable {

    /**
     * Observes whether the reaction can be executed. This observable emits updates
     * to indicate if the conditions required for execution are satisfied.
     *
     * @return An [Observable] emitting true if the reaction van be executed, false otherwise.
     */
    fun canExecute(): Observable<Boolean>

    /**
     * Executes the reactions.
     */
    fun execute()

    /**
     * Activates reactive scheduling after the environment is fully initialized.
     *
     * @param atTime the current simulation time
     * @param environment the initialized environment
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
     * @return the widest context inspected by this actionable's conditions
     */
    val inputContext: Context

    /**
     * @return the widest context modified by this actionable's actions
     */
    val outputContext: Context

    /**
     * Returns the speed of this [Reaction]. It is an average number, and
     * can potentially change during the simulation, depending on the
     * implementation.
     *
     * @return the number of times this [Reaction] is triggered per time
     * unit.
     */
    val rate: Double

    /**
     * @return The global [Time] at which this reaction is scheduled to be
     * executed
     */
    val tau: Observable<Time>

    /**
     * @return the [TimeDistribution] for this [Reaction]
     */
    val timeDistribution: TimeDistribution

    /**
     * Advances this actionable's reaction-owned scheduling state after its scheduled event fires.
     *
     * @param currentTime
     * the current [Time] of execution. This is mandatory in
     * order to correctly compute the time shift of an
     * already-scheduled reaction
     */
    fun update(currentTime: Time)
}
