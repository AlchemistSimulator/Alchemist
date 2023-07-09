/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Holds alchemist launch options configuration values
 */
data class OptionsConfig(
    val variables: List<String> = emptyList(),
    val engineConfig: EngineConfig = EngineConfig(),
    @JsonDeserialize(using = VerbosityDeserializer::class)
    val verbosity: Verbosity = defaultVerbosity,
    val parallelism: Int = defaultParallelism,
    val endTime: Double = defaultEndTime,
    val isWeb: Boolean = false,
    val isBatch: Boolean = false,
    val distributedConfigPath: String? = null,
    val graphicsPath: String? = null,
    val serverConfigPath: String? = null,
) {

    /**
     * Engine configuration.
     */
    data class EngineConfig(
        @JsonDeserialize(using = EngineModeDeserializer::class)
        val engineMode: EngineMode = defaultEngineMode,
        @JsonDeserialize(using = OutputReplayStrategyDeserializer::class)
        val outputReplayStrategy: OutputReplayStrategy = defaultOutputReplayStrategy,
        val batchSize: Int? = null,
        val epsilon: Double = defaultEpsilonSize,
    )

    companion object {
        /**
         * If no specific number of parallel threads to use is specified, this value is used.
         * Defaults to the number of logical cores detected by the JVM.
         */
        val defaultParallelism = Runtime.getRuntime().availableProcessors()

        /**
         * Default final time to be used if no final time is specified.
         * Defaults to [Double.POSITIVE_INFINITY].
         */
        const val defaultEndTime = Double.POSITIVE_INFINITY

        /**
         * Default engine mode to be used if not engine mode is specified.
         * Defaults to [EngineMode.DETERMINISTIC].
         */
        val defaultEngineMode = EngineMode.DETERMINISTIC

        /**
         * Default log verbosity level.
         * Defaults to [Verbosity.WARN]
         */
        val defaultVerbosity = Verbosity.WARN

        /**
         * Default epsilon value used only in epsilon engine mode.
         * Defaults to 0.01
         */
        const val defaultEpsilonSize = 0.01

        /**
         * Default output replay strategy.
         * Defaults to replay
         */
        val defaultOutputReplayStrategy = OutputReplayStrategy.REPLAY
    }
}
