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
import it.unibo.alchemist.model.observation.ObservableSet

/**
 * A prerequisite over model state associated with a [Node].
 *
 * Most reactions observe condition validity to gate scheduling. A reaction with occurrence-time semantics may read
 * the same validity only when it fires, without subscribing to the condition's dependencies.
 *
 * @param T concentration type
 */
interface Condition<T> : Disposable {
    /**
     * Creates an equivalent condition for [newNode] and [newReaction].
     */
    fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): Condition<T>

    /**
     * Observable model values which may affect this condition.
     *
     * Reaction implementations may observe these values to refresh their state and scheduling policy. The engine
     * does not use them to build a dependency graph and observes only the owning reaction's next occurrence.
     */
    fun getDependencies(): ObservableSet<out Observable<*>>

    /**
     * The node owning this condition.
     */
    fun getNode(): Node<T>

    /**
     * The live validity of this condition.
     */
    fun isValid(): Observable<Boolean>

    /**
     * Signals that the owning reaction is about to fire.
     * Used to implement conditions latched to changes that occur in-between reaction executions.
     */
    fun reactionReady() = Unit
}
