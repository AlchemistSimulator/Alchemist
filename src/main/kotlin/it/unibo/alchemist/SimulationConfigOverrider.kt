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
     * _simulation-configuration:
     *   end-time: 50
     *   engine-configuration:
     *     engine-mode: batchEpsilon
     *
     * In order to override each variable we should provide:
     *
     * [
     *   _simulation-configuration.end-time=100
     *   _simulation-configuration.engine-configuration.engine-mode=batchEpsilon
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
                launcher = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.launcherKey}" }?.value
                    ?: config.launcher,
                variables = parseVariables(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.variablesKey}" }?.value)
                    ?: config.variables,
                engineConfig = SimulationConfig.EngineConfig(
                    engineMode = parseEngineMode(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.engineConfigKey}.${SimulationConfig.EngineConfig.engineModeKey}" }?.value)
                        ?: config.engineConfig.engineMode,
                    outputReplayStrategy = parseOutputReplayStrategy(overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.engineConfigKey}.${SimulationConfig.EngineConfig.outputReplayStrategyKey}" }?.value)
                        ?: config.engineConfig.outputReplayStrategy,
                    batchSize = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.engineConfigKey}.${SimulationConfig.EngineConfig.batchSizeKey}" }?.value
                        ?.toInt()
                        ?: config.engineConfig.batchSize,
                    epsilon = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.engineConfigKey}.${SimulationConfig.EngineConfig.epsilonKey}" }?.value
                        ?.toDouble()
                        ?: config.engineConfig.epsilon,
                ),
                parallelism = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.parallelismKey}" }?.value
                    ?.toInt()
                    ?: config.parallelism,
                endTime = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.endTimeKey}" }?.value
                    ?.toDouble()
                    ?: config.endTime,
                isWeb = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.isWebKey}" }?.value
                    ?.toBoolean()
                    ?: config.isWeb,
                isBatch = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.isBatchKey}" }?.value
                    ?.toBoolean()
                    ?: config.isBatch,
                distributedConfigPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.distributedConfigPathKey}" }?.value
                    ?: config.distributedConfigPath,
                graphicsPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.graphicsPathKey}" }?.value
                    ?: config.graphicsPath,
                serverConfigPath = overrides.find { it.key == "${SimulationConfigWrapper.wrapperKey}.${SimulationConfig.serverConfigPathKey}" }?.value
                    ?: config.serverConfigPath,
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
