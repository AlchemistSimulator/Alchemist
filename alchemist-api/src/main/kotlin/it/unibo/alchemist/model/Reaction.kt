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
 * A time-distributed entity whose absolute occurrence time is owned by the entity itself.
 *
 * The engine initializes an actionable before scheduling it, indexes [nextOccurrence], and subscribes to that
 * observable without replaying its current value. After the scheduler selects the occurrence, the engine may call
 * [execute] and always calls [updateAfterFiring] to consume it. Reactive invalidation between occurrences is owned
 * by the implementation and is communicated to the engine only by emitting a new [nextOccurrence].
 */
sealed interface Reaction<T> :
    Comparable<Reaction<T>>,
    Serializable,
    Disposable {

    /**
     *  The list of [Action]s of the [NodeReaction].
     *  Please be careful when you modify this list.
     */
    var actions: List<Action<T>>

    /**
     * The list of [Condition]s of the [NodeReaction].
     * Please be careful when you modify this list.
     */
    var conditions: List<Condition<T>>

    /**
     * Returns the speed of this [NodeReaction]. It is an average number, and
     * can potentially change during the simulation, depending on the
     * implementation.
     *
     * @return the number of times this [NodeReaction] is triggered per time
     * unit.
     */
    val rate: Double

    /**
     * The absolute [Time] of the next occurrence.
     *
     * This is the sole observable used by the engine for scheduling. Once the actionable has been registered, every
     * emission requests scheduler reindexing; changing other observable state does not directly notify the engine.
     */
    val nextOccurrence: Observable<Time>

    /**
     * @return the [TimeDistribution] for this [NodeReaction]
     */
    val timeDistribution: TimeDistribution<T>

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
     * Activates reactive inputs after the environment is fully initialized and establishes the first occurrence.
     *
     * The engine invokes this method exactly once before indexing [nextOccurrence] in its scheduler.
     *
     * @param atTime the current simulation time
     * @param environment the initialized environment
     */
    fun initializationComplete(atTime: Time, environment: Environment<T, *>)

    /**
     * Consumes the occurrence at [currentTime], refreshing reaction state and applying post-firing scheduling policy.
     *
     * This transition is required even when [execute] was skipped because [canExecute] was false. It is distinct
     * from a reactive invalidation that happens between scheduled occurrences.
     *
     * @param currentTime
     * the [Time] at which the scheduled occurrence fired
     */
    fun updateAfterFiring(currentTime: Time)
}
