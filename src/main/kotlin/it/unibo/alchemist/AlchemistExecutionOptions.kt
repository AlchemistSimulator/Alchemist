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
 * Alchemist options provided by the command line.
 *
 * @property configuration null if no simulation file is specified (default), file path otherwise
 * @property headless true if forced, false otherwise (default)
 * @property variables selected batch variables. Defaults to [emptyList]
 * @property batch whether batch mode is selected.
 * @property distributed the path to the file with the load distribution configuration, or null if the run is local
 * @property graphics the path to the effects file, or null if unspecified
 * @property fxui whether the JavaFX UI takes priority over the default Swing UI
 * @property help true if print help function is selected
 * @property interval sampling time, defaults to [defaultInterval]
 * @property server if launched as Alchemist grid node server, the path to the configuration file. Null otherwise.
 * @property parallelism parallel threads used for running locally. Defaults to [defaultParallelism]
 * @property endTime final simulation time. Defaults to [defaultEndTime]
 */
data class AlchemistExecutionOptions(
    val configuration: String? = null,
    val headless: Boolean = false,
    val variables: List<String> = emptyList(),
    val batch: Boolean = false,
    val distributed: String? = null,
    val graphics: String? = null,
    val fxui: Boolean = false,
    val help: Boolean = false,
    val interval: Double = defaultInterval,
    val server: String? = null,
    val parallelism: Int = defaultParallelism,
    val endTime: Double = defaultEndTime
) {
    /**
     * returns true if all options are set to their default value.
     */
    val isEmpty: Boolean get() = this == NO_OPTION
    companion object {
        /**
         * If no sampling interval is specified, this option value is used. Defaults to 1.0.
         */
        const val defaultInterval = 1.0
        /**
         * If no specific number of parallel threads to use is specified, this value is used.
         * Defaults to the number of logical cores detected by the JVM.
         */
        val defaultParallelism = Runtime.getRuntime().availableProcessors()
        /**
         * Default final time to be used if no final time is specified.
         * Defaults to [Double.POSITIVE_INFINITY].
         */
        val defaultEndTime = Double.POSITIVE_INFINITY

        private val NO_OPTION = AlchemistExecutionOptions()
    }
}
