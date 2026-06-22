/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.observation.Observable
import java.io.Serializable
import javax.annotation.Nonnull

/**
 * This interface represents a temporal distribution for any event.
 *
 * @param <T>
 * concentration type
</T> */
interface TimeDistribution<T> {
    /**
     * Updates the internal status.
     *
     * @param currentTime
     * current time
     * @param executed
     * true if the reaction has just been executed
     * @param source
     * the host reaction
     * @param environment
     * the current environment
     */
    fun update(currentTime: Time, executed: Boolean, source: Actionable<T>)

    /**
     * @return the next time at which the event will occur
     */
    val nextOccurence: Observable<Time>

    /**
     * @return how many times per time unit the event will happen on average
     */
    val rate: Double

    /**
     * @param destination the node where the newly created time distribution will be placed
     * @param currentTime
     * the time at which the cloning operation happened
     * @return an exact copy of this [TimeDistribution]
     */
    fun cloneOnNewNode(@Nonnull destination: Node<T?>, @Nonnull currentTime: Time): TimeDistribution<T?>?
}
