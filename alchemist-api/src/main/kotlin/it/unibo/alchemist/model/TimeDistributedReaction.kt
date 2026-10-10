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
 * A [Reaction] whose putative occurrence delays are sampled from a [timeDistribution].
 *
 * Most time-distributed reactions recur. A single-use implementation may instead remove itself after its first
 * successful execution. Both expose their sampler and an average execution [rate] when one is available.
 */
interface TimeDistributedReaction<T> : Reaction<T> {
    /** The average number of occurrences per time unit, or `NaN` when unavailable. */
    val rate: Double

    /** The delay distribution governing this reaction's putative occurrences. */
    val timeDistribution: TimeDistribution<T>

    /**
     * Advances distribution-backed scheduling after successful execution at [currentTime].
     *
     * Normal scheduled execution invokes this through [Reaction.execute].
     * A single-use reaction removes itself during execution, so their execution never reaches this operation.
     */
    fun updateSchedulingAfterFiring(currentTime: Time)
}
