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
 * A prerequisite over model state owned by a [Reaction].
 *
 * Recurring reactions normally observe condition validity to gate scheduling. A reaction with occurrence-time
 * semantics may read the same validity only when it fires. Reaction families requiring additional scheduling state
 * subscribe directly to their narrow semantic inputs.
 *
 * @param T concentration type
 */
interface Condition<T> : Disposable {
    /**
     * The reaction owning this condition.
     * Its [Reaction.host] is the model entity hosting the condition, which may differ from the node it targets, if any.
     */
    val reaction: Reaction<T>

    /**
     * Creates an equivalent condition owned by [newReaction], such as a reaction cloned onto a new node.
     * A condition targeting the host of its reaction targets the host of [newReaction], which must then be a [Node];
     * a condition targeting any other node keeps its target.
     */
    fun cloneCondition(newReaction: Reaction<T>): Condition<T>

    /**
     * The live validity of this condition.
     */
    val isValid: Observable<Boolean>

    /**
     * Signals that the owning reaction is about to fire.
     * Used to implement conditions latched to changes that occur in-between reaction executions.
     */
    fun beforeReactionFires() = Unit
}
