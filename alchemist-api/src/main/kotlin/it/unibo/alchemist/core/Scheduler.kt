/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.model.Reaction

/**
 * The data structure(s) managing the reactions to be executed.
 *
 * @param T concentration type
 */
interface Scheduler<T> {
    /**
     * The next reaction to be executed, or null if there is none.
     */
    val next: Reaction<T>?

    /**
     * Adds [reaction] to the data structure.
     */
    fun addReaction(reaction: Reaction<T>)

    /**
     * Removes [reaction] from the structure. Fails if the reaction is not present.
     */
    fun removeReaction(reaction: Reaction<T>)

    /**
     * Notifies the structure that [reaction] has changed.
     * The whole structure will be rearranged to ensure consistency.
     */
    fun updateReaction(reaction: Reaction<T>)
}
