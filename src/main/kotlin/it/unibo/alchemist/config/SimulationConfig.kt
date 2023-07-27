/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.config

import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.AlchemistExecutionOptions.Companion.defaultEndTime
import it.unibo.alchemist.AlchemistExecutionOptions.Companion.defaultParallelism
import it.unibo.alchemist.config.SimulationConfig.SimulationConfigParameters.Companion.defaultEndTime
import it.unibo.alchemist.config.SimulationConfig.SimulationConfigParameters.Companion.defaultParallelism
import it.unibo.alchemist.config.VariablesParsingUtils.parse
import it.unibo.alchemist.config.VariablesParsingUtils.parseBoolean
import it.unibo.alchemist.config.VariablesParsingUtils.parseDouble
import it.unibo.alchemist.config.VariablesParsingUtils.parseInt
import it.unibo.alchemist.config.VariablesParsingUtils.parseMap
import it.unibo.alchemist.config.VariablesParsingUtils.parseString
import it.unibo.alchemist.config.VariablesParsingUtils.parseStringList

/**
 * Holds alchemist launch options configuration values.
 *
 * @property type launcher type
 * @property parameters launcher parameters
 */
data class SimulationConfig(
    val type: String = defaultLauncherType,
    val parameters: SimulationConfigParameters,
) {

    /**
     * @property variables selected batch variables. Defaults to [emptyList]
     * @property parallelism parallel threads used for running locally. Defaults to [defaultParallelism]
     * @property endTime final simulation time. Defaults to [defaultEndTime]
     * @property isWeb true if the web renderer is used. Defaults to false.
     * @property isBatch whether batch mode is selected.
     * @property distributedConfigPath the path to the file with the load distribution configuration,
     * or null if the run is local
     * @property graphicsPath the path to the effects file, or null if unspecified
     * @property serverConfigPath if launched as Alchemist grid node server,
     * the path to the configuration file. Null otherwise.
     * @property engineMode engine execution mode
     * @property outputReplayStrategy batch engine mode output replay strategy
     * @property batchSize batch engine mode fixed batch size
     * @property epsilon epsilon engine mode epsilon threshold value
     */
    data class SimulationConfigParameters(
        val variables: List<String>,
        val parallelism: Int,
        val endTime: Double,
        val isWeb: Boolean,
        val isBatch: Boolean,
        val distributedConfigPath: String?,
        val graphicsPath: String?,
        val serverConfigPath: String?,
        val engineMode: EngineMode,
        val outputReplayStrategy: OutputReplayStrategy,
        val batchSize: Int,
        val epsilon: Double,
    ) {
        companion object {
            /**
             * If no specific number of parallel threads to use is specified, this value is used.
             * Defaults to the number of logical cores detected by the JVM.
             */
            private val defaultParallelism = Runtime.getRuntime().availableProcessors()

            /**
             * Default final time to be used if no final time is specified.
             * Defaults to [Double.POSITIVE_INFINITY].
             */
            private const val defaultEndTime = Double.POSITIVE_INFINITY

            /**
             * Default engine mode to be used if not engine mode is specified.
             * Defaults to [EngineMode.DETERMINISTIC].
             */
            private val defaultEngineMode = EngineMode.DETERMINISTIC

            /**
             * Default epsilon value used only in epsilon engine mode.
             * Defaults to 0.01
             */
            private const val defaultEpsilonSize = 0.01

            /**
             * Default output replay strategy.
             * Defaults to replay
             */
            private val defaultOutputReplayStrategy = OutputReplayStrategy.REPLAY

            private const val variablesKey = "variables"
            private const val parallelismKey = "parallelism"
            private const val endTimeKey = "end-time"
            private const val isWebKey = "is-web"
            private const val isBatchKey = "is-batch"
            private const val distributedConfigPathKey = "distributed-configuration-path"
            private const val graphicsPathKey = "graphics-path"
            private const val serverConfigPathKey = "server-configuration-path"
            private const val engineModeKey = "engine-mode"
            private const val outputReplayStrategyKey = "output-replay-strategy"
            private const val batchSizeKey = "batch-size"
            private const val epsilonKey = "epsilon"

            /**
             * Construct a [SimulationConfigParameters] from parsed variables map.
             */
            fun fromVariables(variables: Map<String, Any?>?): SimulationConfigParameters {
                return SimulationConfigParameters(
                    variables = variables?.get(parametersKey)?.parseMap()?.get(variablesKey).parseStringList()
                        .orEmpty(),
                    parallelism = variables?.get(parametersKey)?.parseMap()?.get(parallelismKey).parseInt()
                        ?: defaultParallelism,
                    endTime = variables?.get(parametersKey)?.parseMap()?.get(endTimeKey).parseDouble()
                        ?: defaultEndTime,
                    isWeb = variables?.get(parametersKey)?.parseMap()?.get(isWebKey).parseBoolean() ?: false,
                    isBatch = variables?.get(parametersKey)?.parseMap()?.get(isBatchKey).parseBoolean() ?: false,
                    distributedConfigPath = variables?.get(parametersKey)?.parseMap()?.get(distributedConfigPathKey)
                        .parseString(),
                    graphicsPath = variables?.get(parametersKey)?.parseMap()?.get(graphicsPathKey).parseString(),
                    serverConfigPath = variables?.get(parametersKey)?.parseMap()?.get(serverConfigPathKey)
                        .parseString(),
                    engineMode = variables?.get(parametersKey)?.parseMap()?.get(engineModeKey).parse {
                        EngineMode.parseCode(it)
                    } ?: defaultEngineMode,
                    outputReplayStrategy =
                    variables?.get(parametersKey)?.parseMap()?.get(outputReplayStrategyKey).parse {
                        OutputReplayStrategy.parseCode(it)
                    } ?: defaultOutputReplayStrategy,
                    batchSize = variables?.get(parametersKey)?.parseMap()?.get(batchSizeKey).parseInt()
                        ?: defaultParallelism,
                    epsilon = variables?.get(parametersKey)?.parseMap()?.get(epsilonKey).parseDouble()
                        ?: defaultEpsilonSize,
                )
            }
        }
    }

    /**
     * Construct a [AlchemistExecutionOptions] from this.
     *
     * @property simulationFile path to simulation file
     * @property overrides list of valid yaml strings to be applied as overrides
     */
    fun toLegacy(simulationFile: String, overrides: List<String>): AlchemistExecutionOptions {
        return AlchemistExecutionOptions(
            configuration = simulationFile,
            headless = this.type == defaultLauncherType,
            variables = this.parameters.variables,
            overrides = overrides,
            batch = this.parameters.isBatch,
            distributed = this.parameters.distributedConfigPath,
            graphics = this.parameters.graphicsPath,
            fxui = this.type == javaFxLauncherType,
            web = this.parameters.isWeb,
            help = false,
            server = this.parameters.serverConfigPath,
            parallelism = this.parameters.parallelism,
            endTime = this.parameters.endTime,
            engineConfig = AlchemistExecutionOptions.Companion.EngineConfig(
                engineMode = this.parameters.engineMode,
                outputReplayStrategy = this.parameters.outputReplayStrategy,
                batchSize = this.parameters.batchSize,
                epsilon = this.parameters.epsilon,
            ),
        )
    }

    companion object {
        /**
         * Default launcher type.
         * Defaults to HeadlessSimulationLauncher
         */
        private const val defaultLauncherType = "HeadlessSimulationLauncher"
        private const val javaFxLauncherType = "SingleRunFXUI"

        private const val rootKey = "launcher"
        private const val typeKey = "type"
        private const val parametersKey = "parameters"

        /**
         * Construct [SimulationConfig] from map of variables.
         *
         * @property variables map of variables
         */
        fun fromVariables(variables: Map<String, Any>): SimulationConfig {
            return SimulationConfig(
                type = variables[rootKey]?.parseMap()?.get(typeKey)?.parseString() ?: defaultLauncherType,
                parameters = SimulationConfigParameters.fromVariables(variables[rootKey]?.parseMap()),
            )
        }
    }
}
