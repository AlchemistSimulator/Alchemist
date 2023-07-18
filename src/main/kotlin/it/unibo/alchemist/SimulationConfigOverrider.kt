/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

/**
 * Use this to override _engine-configuration values in simulation file.
 */
object SimulationConfigOverrider {

    /**
     * Override options with a list of resolvable key-value pairs.
     *
     * For example, given the following yml file parsed to a map of maps (and lists):
     *
     * simulation-configuration:
     *   end-time: 50
     *   engine-configuration:
     *     engine-mode: batchEpsilon
     *
     * In order to override each variable we should provide:
     *
     * [
     *   launcher.end-time=100
     *   simulation-configuration.engine-configuration.engine-mode=batchEpsilon
     * ]
     */
    @JvmStatic
    fun overrideOptionsFile(config: SimulationConfig, stringOverrides: List<String>): SimulationConfig {
        return if (stringOverrides.isEmpty()) {
            config
        } else {
            data class Override(
                val key: String,
                val value: String,
            )

            val overrides = stringOverrides.map {
                Override(
                    key = it.substringBefore("="),
                    value = it.substringAfter("="),
                )
            }

            SimulationConfig(
                type = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.typeKey}" }?.value
                    ?: config.type,
                parameters = SimulationConfig.SimulationConfigParameters(
                    variables = parseVariables(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.variablesKey}" }?.value)
                        ?: config.parameters.variables,
                    parallelism = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.parallelismKey}" }?.value
                        ?.toInt()
                        ?: config.parameters.parallelism,
                    endTime = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.endTimeKey}" }?.value
                        ?.toDouble()
                        ?: config.parameters.endTime,
                    isWeb = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.isWebKey}" }?.value
                        ?.toBoolean()
                        ?: config.parameters.isWeb,
                    isBatch = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.isBatchKey}" }?.value
                        ?.toBoolean()
                        ?: config.parameters.isBatch,
                    distributedConfigPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.distributedConfigPathKey}" }?.value
                        ?: config.parameters.distributedConfigPath,
                    graphicsPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.graphicsPathKey}" }?.value
                        ?: config.parameters.graphicsPath,
                    serverConfigPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.serverConfigPathKey}" }?.value
                        ?: config.parameters.serverConfigPath,
                    engineMode = parseEngineMode(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.engineModeKey}" }?.value)
                        ?: config.parameters.engineMode,
                    outputReplayStrategy = parseOutputReplayStrategy(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.outputReplayStrategyKey}" }?.value)
                        ?: config.parameters.outputReplayStrategy,
                    batchSize = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.batchSizeKey}" }?.value
                        ?.toInt()
                        ?: config.parameters.batchSize,
                    epsilon = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.SimulationConfigParameters.epsilonKey}" }?.value
                        ?.toDouble()
                        ?: config.parameters.epsilon,
                ),
            )
        }
    }

    private fun parseVariables(value: String?): List<String>? {
        return value?.substringAfter("[")?.substringBefore("]")?.replace(" ", "")?.split(",")
    }

    private fun parseEngineMode(value: String?): EngineMode? {
        return value?.let { v -> EngineMode.values().find { it.code == v } }
    }

    private fun parseOutputReplayStrategy(value: String?): OutputReplayStrategy? {
        return value?.let { v -> OutputReplayStrategy.values().find { it.code == v } }
    }
}
