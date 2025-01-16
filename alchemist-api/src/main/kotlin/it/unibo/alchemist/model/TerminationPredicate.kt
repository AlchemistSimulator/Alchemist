/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.TerminationPredicate.Companion.invoke
import java.io.Serializable

/**
 * A predicate that determines whether a simulation should be terminated.
 * The predicate is evaluated at the end of each simulation step.
 * If the predicate returns true, the simulation gets terminated.
 * @param T the type of the concentration
 * @param P the type of the position
 */
interface TerminationPredicate<T, P : Position<out P>> :
    (Environment<T, P>) -> Boolean,
    Serializable {
    override fun invoke(environment: Environment<T, P>): Boolean

    /**
     * Same as [invoke].
     */
    fun test(environment: Environment<T, P>): Boolean = invoke(environment)

    /**
     * Builds a predicate that is true if both `this` and the [other] predicate are true.
     */
    fun or(other: TerminationPredicate<T, P>): TerminationPredicate<T, P> =
        TerminationPredicate<T, P> {
            this(it) || other(it)
        }

    /**
     * Factory for [TerminationPredicate] instances.
     */
    companion object {
        /**
         * Creates a [TerminationPredicate] from a predicate.
         * @param predicate the predicate to use
         * @return a [TerminationPredicate] that uses the given predicate
         */
        operator fun <T, P : Position<out P>> invoke(
            predicate: (Environment<T, P>) -> Boolean,
        ): TerminationPredicate<T, P> =
            object : TerminationPredicate<T, P> {
                override fun invoke(environment: Environment<T, P>): Boolean = predicate(environment)
            }
    }
}
