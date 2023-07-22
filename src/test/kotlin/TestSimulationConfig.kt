import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.boundary.modelproviders.YamlProvider
import it.unibo.alchemist.config.EngineMode
import it.unibo.alchemist.config.OutputReplayStrategy
import it.unibo.alchemist.config.SimulationConfig
import org.kaikikm.threadresloader.ResourceLoader

/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestSimulationConfig : StringSpec({

    "test fromVariables" {
        val resource = ResourceLoader.getResource("config/config-test.yml")
        val variables = YamlProvider.from(resource)

        val expected = SimulationConfig(
            type = "TestLauncher",
            parameters = SimulationConfig.SimulationConfigParameters(
                variables = listOf("1", "2", "a"),
                parallelism = 8,
                endTime = 50.0,
                isWeb = true,
                isBatch = true,
                distributedConfigPath = "foo-bar",
                graphicsPath = "foo-bar",
                serverConfigPath = "foo-bar",
                engineMode = EngineMode.BATCH_EPSILON,
                batchSize = 10,
                epsilon = 0.01,
                outputReplayStrategy = OutputReplayStrategy.REPLAY,
            ),
        )

        val result = SimulationConfig.fromVariables(variables)

        result shouldBe expected
    }

    "test toLegacy" {
        val config = SimulationConfig(
            type = "HeadlessSimulationLauncher",
            parameters = SimulationConfig.SimulationConfigParameters(
                variables = listOf("1", "2", "a"),
                parallelism = 8,
                endTime = 50.0,
                isWeb = true,
                isBatch = true,
                distributedConfigPath = "foo-bar1",
                graphicsPath = "foo-bar2",
                serverConfigPath = "foo-bar3",
                engineMode = EngineMode.BATCH_EPSILON,
                batchSize = 10,
                epsilon = 0.01,
                outputReplayStrategy = OutputReplayStrategy.REPLAY,
            ),
        )
        val expected = AlchemistExecutionOptions(
            configuration = "test",
            headless = true,
            variables = listOf("1", "2", "a"),
            overrides = emptyList(),
            batch = true,
            distributed = "foo-bar1",
            graphics = "foo-bar2",
            fxui = false,
            web = true,
            help = false,
            server = "foo-bar3",
            parallelism = 8,
            endTime = 50.0,
            engineConfig = AlchemistExecutionOptions.Companion.EngineConfig(
                engineMode = EngineMode.BATCH_EPSILON,
                outputReplayStrategy = OutputReplayStrategy.REPLAY,
                batchSize = 10,
                epsilon = 0.01,
            ),
        )

        config.toLegacy("test", emptyList()) shouldBe expected
    }
})
