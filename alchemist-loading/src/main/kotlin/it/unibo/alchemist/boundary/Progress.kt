/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import kotlinx.coroutines.flow.SharedFlow

/**
 * A measure of progress of an Alchemist execution.
 * @param ProgressMeasure the type of the measure
 */
interface Progress<ProgressMeasure : Any> {

    /**
     * @return the current progress
     */
    val current: ProgressMeasure

    /**
     * The maximum progress. When reached, the execution is terminated.
     */
    val expected: ProgressMeasure

    /**
     * @return a [SharedFlow] that emits events when the progress changes
     */
    val events: SharedFlow<ProgressMeasure>

    /**
     * This function returns when the execution is terminated.
     */
    fun awaitTermination()
}
