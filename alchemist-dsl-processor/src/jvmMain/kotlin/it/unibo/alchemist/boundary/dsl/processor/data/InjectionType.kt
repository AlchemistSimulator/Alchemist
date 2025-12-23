/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

/**
 * Types of parameters that can be injected from context.
 */
internal enum class InjectionType {
    /** Environment parameter injection. */
    ENVIRONMENT,

    /** Random generator parameter injection. */
    GENERATOR,

    /** Incarnation parameter injection. */
    INCARNATION,

    /** Node parameter injection. */
    NODE,

    /** Reaction parameter injection. */
    REACTION,

    /** Time distribution parameter injection. */
    TIMEDISTRIBUTION,

    /** Position-based filter parameter injection. */
    FILTER,
}
