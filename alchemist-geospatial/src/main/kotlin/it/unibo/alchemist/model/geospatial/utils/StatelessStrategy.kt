/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.utils

/**
 * Base for spatial/temporal/spatio-temporal strategies that hold **no configuration state**.
 *
 * Provides [equals] and [hashCode] based solely on the type: two instances of the
 * same stateless strategy are always equal, since they behave identically regardless of how
 * or when they were constructed.
 *
 * Only for strategies with **no** primary-constructor parameters. A strategy that carries
 * actual configuration should be a `data class` instead, so equality reflects its configuration,
 * not just its type.
 */
open class StatelessStrategy protected constructor() {

    override fun equals(other: Any?): Boolean = other != null && this::class == other::class

    override fun hashCode(): Int = this::class.hashCode()
}
