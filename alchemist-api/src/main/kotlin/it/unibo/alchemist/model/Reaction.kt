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
 * observable without replaying its current value.
 * After the scheduler selects a finite occurrence, the engine calls [execute].
 * Reactive invalidation and post-execution scheduling are owned by the implementation and are
 * communicated to the engine only by emitting a new [nextOccurrence].
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
     * The [Condition]s interpreted by this reaction's scheduling and execution policy.
     *
     * Conditions normally gate scheduling.
     * A reaction with occurrence-time semantics may instead evaluate them only when its occurrence is selected.
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
     * Observes whether the reaction procedure can be selected and executed by the engine. This observable emits
     * updates when that state changes. Reactions whose procedure includes checking occurrence-time conditions may
     * remain executable even when those conditions will suppress their model effects.
     *
     * @return An [Observable] emitting true if the reaction can be executed, false otherwise.
     */
    fun canExecute(): Observable<Boolean>

    /**
     * Executes this reaction.
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
}
