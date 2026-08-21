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
 * A reactive prerequisite for a [NodeReaction].
 *
 * @param T concentration type
 */
interface Condition<T> : Disposable {
    /**
     * Creates an equivalent condition for [node] and [reaction].
     */
    fun cloneCondition(node: Node<T>, reaction: NodeReaction<T>): Condition<T>

    /**
     * The context inspected by this condition.
     */
    fun getContext(): Context = Context.LOCAL

    /**
     * Observable model values which may affect this condition or its propensity contribution.
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
     * The current contribution used by propensity-aware reaction scheduling policies.
     */
    fun getPropensityContribution(): Observable<Double>

    /**
     * The live validity of this condition.
     */
    fun isValid(): Observable<Boolean>

    /**
     * Signals that the owning reaction is about to execute.
     */
    fun reactionReady() = Unit
}
