/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Holds alchemist launch options configuration values
 */
data class SimulationConfig(
    @JsonProperty("launcher")
    val launcher: String = "HeadlessSimulationLauncher",
    @JsonProperty("variables")
    val variables: List<String> = emptyList(),
    @JsonProperty("engine-config")
    val engineConfig: EngineConfig = EngineConfig(),
    @JsonProperty("parallelism")
    val parallelism: Int = defaultParallelism,
    @JsonProperty("end-time")
    val endTime: Double = defaultEndTime,
    @JsonProperty("is-web")
    val isWeb: Boolean = false,
    @JsonProperty("is-batch")
    val isBatch: Boolean = false,
    @JsonProperty("distributed-config-path")
    val distributedConfigPath: String? = null,
    @JsonProperty("graphics-path")
    val graphicsPath: String? = null,
    @JsonProperty("server-config-path")
    val serverConfigPath: String? = null,
) {

    /**
     * Engine configuration.
     */
    data class EngineConfig(
        @JsonProperty("engine-mode")
        @JsonDeserialize(using = EngineModeDeserializer::class)
        val engineMode: EngineMode = defaultEngineMode,
        @JsonProperty("output-replay-strategy")
        @JsonDeserialize(using = OutputReplayStrategyDeserializer::class)
        val outputReplayStrategy: OutputReplayStrategy = defaultOutputReplayStrategy,
        @JsonProperty("batch-size")
        val batchSize: Int? = null,
        @JsonProperty("epsilon")
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
