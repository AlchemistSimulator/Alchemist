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
    @JsonProperty(launcherKey)
    val launcher: String = defaultLauncher,
    @JsonProperty(variablesKey)
    val variables: List<String> = emptyList(),
    @JsonProperty(engineConfigKey)
    val engineConfig: EngineConfig = EngineConfig(),
    @JsonProperty(parallelismKey)
    val parallelism: Int = defaultParallelism,
    @JsonProperty(endTimeKey)
    val endTime: Double = defaultEndTime,
    @JsonProperty(isWebKey)
    val isWeb: Boolean = false,
    @JsonProperty(isBatchKey)
    val isBatch: Boolean = false,
    @JsonProperty(distributedConfigPathKey)
    val distributedConfigPath: String? = null,
    @JsonProperty(graphicsPathKey)
    val graphicsPath: String? = null,
    @JsonProperty(serverConfigPathKey)
    val serverConfigPath: String? = null,
) {

    /**
     * Engine configuration.
     */
    data class EngineConfig(
        @JsonProperty(engineModeKey)
        @JsonDeserialize(using = EngineModeDeserializer::class)
        val engineMode: EngineMode = defaultEngineMode,
        @JsonProperty(outputReplayStrategyKey)
        @JsonDeserialize(using = OutputReplayStrategyDeserializer::class)
        val outputReplayStrategy: OutputReplayStrategy = defaultOutputReplayStrategy,
        @JsonProperty(batchSizeKey)
        val batchSize: Int? = null,
        @JsonProperty(epsilonKey)
        val epsilon: Double = defaultEpsilonSize,
    ) {
        companion object {
            const val engineModeKey = "engine-mode"
            const val outputReplayStrategyKey = "output-replay-strategy"
            const val batchSizeKey = "batch-size"
            const val epsilonKey = "epsilon"
        }
    }

    companion object {
        /**
         * If not specific launcher is specified, this value is used.
         * Defaults to HeadlessSimulationLauncher
         */
        const val defaultLauncher = "HeadlessSimulationLauncher"

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

        const val launcherKey = "launcher"
        const val variablesKey = "variables"
        const val engineConfigKey = "engine-configuration"
        const val parallelismKey = "parallelism"
        const val endTimeKey = "end-time"
        const val isWebKey = "is-web"
        const val isBatchKey = "is-batch"
        const val distributedConfigPathKey = "distributed-configuration-path"
        const val graphicsPathKey = "graphics-path"
        const val serverConfigPathKey = "server-configuration-path"
    }
}
