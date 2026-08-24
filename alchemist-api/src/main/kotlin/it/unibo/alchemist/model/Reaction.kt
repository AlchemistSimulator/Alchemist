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

/**
 * A scheduled entity whose absolute occurrence time is owned by the entity itself.
 *
 * The engine initializes a reaction before scheduling it, indexes [nextOccurrence], and subscribes to that
 * observable without replaying its current value. After the scheduler selects the occurrence, the engine may call
 * [execute], then temporarily calls [updateSchedulingAfterFiring] to consume that selected occurrence. Reactive
 * invalidation between occurrences is owned by the implementation and is communicated to the engine only by
 * emitting a new [nextOccurrence].
 */
interface Reaction<T> :
    Comparable<Reaction<T>>,
    Disposable {

    /**
     * The [Action]s executed by this reaction.
     * Please be careful when modifying this list.
     */
    var actions: List<Action<T>>

    /**
     * The [Condition]s controlling whether this reaction can execute.
     * Please be careful when modifying this list.
     */
    var conditions: List<Condition<T>>

    /**
     * The absolute [Time] of the next occurrence.
     *
     * This is the sole observable used by the engine for scheduling. Once the reaction has been registered, every
     * emission requests scheduler reindexing; changing other observable state does not directly notify the engine.
     */
    val nextOccurrence: Observable<Time>

    /**
     * Observes whether the reaction can be executed. This observable emits updates
     * to indicate whether the conditions required for execution are satisfied.
     *
     * @return An [Observable] emitting true if the reaction can be executed, false otherwise.
     */
    fun canExecute(): Observable<Boolean>

    /**
     * Executes this reaction.
     */
    fun execute()

    /**
     * Consumes the occurrence selected at [currentTime].
     *
     * This hook is temporarily part of the root contract while a selected reaction whose conditions became invalid
     * must still advance stateful scheduling state. Once condition validity directly forces [nextOccurrence] to
     * infinity, successful recurring reactions will own this transition and this method will be removed.
     */
    fun updateSchedulingAfterFiring(currentTime: Time)

    /**
     * Activates reactive inputs after the environment is fully initialized and establishes the first occurrence.
     *
     * The engine invokes this method exactly once before indexing [nextOccurrence] in its scheduler.
     *
     * @param atTime the current simulation time
     * @param environment the initialized environment
     */
    fun initializationComplete(atTime: Time, environment: Environment<T, *>)
}
