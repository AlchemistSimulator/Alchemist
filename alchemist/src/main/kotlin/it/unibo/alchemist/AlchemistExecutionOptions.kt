/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

/**
 * @param configuration null if no simulation file is specified (default), file path otherwise
 * @param headless true if forced, false otherwise (default)
 */
data class AlchemistExecutionOptions(
    val configuration: String? = null,
    val headless: Boolean = false,
    val variables: List<String> = emptyList(),
    val batch: Boolean = false,
    val export: String? = null,
    val distributed: String? = null,
    val graphics: String? = null,
    val help: Boolean = false,
    val interval: Double = defaultInterval,
    val server: String? = null,
    val parallelism: Int = defaultParallelism,
    val endTime: Double = defaultEndTime
) {
    companion object {
        const val defaultInterval = 1.0
        val defaultParallelism = Runtime.getRuntime().availableProcessors()
        val defaultEndTime = Double.POSITIVE_INFINITY
    }
}
