/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate

/**
 * DSL utilities for registering [TerminationPredicate] instances in an [Environment].
 *
 * This context is meant to be used with Kotlin context receivers, requiring an [Environment] to be available
 * in the surrounding scope. The unary minus operator provides a compact notation for adding terminators.
 */
object TerminatorsContext {

    /**
     * Registers this [TerminationPredicate] into the current [Environment].
     *
     * This operator enables concise DSL statements by interpreting `-predicate` as
     * "add this predicate as a terminator to the current environment".
     *
     * @receiver the termination predicate to register.
     */
    context(environment: Environment<T, P>)
    operator fun <T, P : Position<P>> TerminationPredicate<T, P>.unaryMinus() = environment.addTerminator(this)
}
