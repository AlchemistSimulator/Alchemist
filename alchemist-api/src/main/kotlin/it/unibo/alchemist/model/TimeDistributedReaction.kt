/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * A recurring [Reaction] whose delays are sampled from a [timeDistribution].
 *
 * A time-distributed reaction exposes the sampler governing recurrence and an average execution [rate] when its
 * implementation can provide one.
 */
interface TimeDistributedReaction<T> : Reaction<T> {
    /** The average number of occurrences per time unit, or `NaN` when unavailable. */
    val rate: Double

    /** The delay distribution governing this reaction's recurrence. */
    val timeDistribution: TimeDistribution<T>
}
