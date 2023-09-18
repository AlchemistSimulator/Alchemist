/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

enum class TimeSurrogate {
    ZERO,
    INFINITY,
    NEGATIVE_INFINITY,
}

/**
 * Maps a [TimeSurrogate] into a [it.unibo.alchemist.model.Time].
 */
fun TimeSurrogate.toAlchemistTime(): it.unibo.alchemist.model.Time = when (this) {
    TimeSurrogate.ZERO -> it.unibo.alchemist.model.Time.ZERO
    TimeSurrogate.INFINITY -> it.unibo.alchemist.model.Time.INFINITY
    TimeSurrogate.NEGATIVE_INFINITY -> it.unibo.alchemist.model.Time.NEGATIVE_INFINITY
}