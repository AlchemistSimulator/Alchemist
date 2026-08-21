/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Reaction

/**
 * DSL entry-point for configuring an [Reaction] by attaching [Action]s and [Condition]s.
 *
 * This object is intended to be used with Kotlin context receivers, so that the target [Reaction] instance
 * is implicitly available in the current scope.
 *
 * Both [action] and [condition] mutate the current [Reaction] by appending the provided element to its internal
 * collections. Ordering is preserved and duplicates are allowed.
 */
object ActionableContext {

    /**
     * Appends the given [Action] to the current [Reaction].
     *
     * @param action the action to add.
     */
    context(reaction: Reaction<T>)
    fun <T> action(action: Action<T>) {
        reaction.actions += action
    }

    /**
     * Appends the given [Condition] to the current [Reaction].
     *
     * @param condition the condition to add.
     */
    context(reaction: Reaction<T>)
    fun <T> condition(condition: Condition<T>) {
        reaction.conditions += condition
    }
}
