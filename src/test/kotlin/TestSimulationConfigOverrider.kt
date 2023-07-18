import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.EngineMode
import it.unibo.alchemist.OutputReplayStrategy
import it.unibo.alchemist.SimulationConfig
import it.unibo.alchemist.SimulationConfigOverrider
import it.unibo.alchemist.SimulationConfigWrapper
import org.kaikikm.threadresloader.ResourceLoader

/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestSimulationConfigOverrider : StringSpec({

    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    "test override" {

        val resource = ResourceLoader.getResource("override-test.yml")
        val config = mapper.readValue(resource, SimulationConfigWrapper::class.java).simulationConfiguration

        val overrides = listOf(
            "simulation-configuration.launcher=TestLauncher",
            "simulation-configuration.variables=[10, 12, 13]",
            "simulation-configuration.parallelism=10",
            "simulation-configuration.end-time=100.5",
            "simulation-configuration.is-web=false",
            "simulation-configuration.is-batch=false",
            "simulation-configuration.distributed-configuration-path=test",
            "simulation-configuration.graphics-path=test",
            "simulation-configuration.server-configuration-path=test",
            "simulation-configuration.engine-configuration.engine-mode=batchFixed",
            "simulation-configuration.engine-configuration.epsilon=0.0001",
            "simulation-configuration.engine-configuration.output-replay-strategy=aggregate",
        )
        val expected = SimulationConfig(
            launcher = "TestLauncher",
            variables = listOf("10", "12", "13"),
            parallelism = 10,
            endTime = 100.5,
            isWeb = false,
            isBatch = false,
            distributedConfigPath = "test",
            graphicsPath = "test",
            serverConfigPath = "test",
            engineConfig = SimulationConfig.EngineConfig(
                engineMode = EngineMode.BATCH_FIXED,
                epsilon = 0.0001,
                outputReplayStrategy = OutputReplayStrategy.AGGREGATE,
            ),
        )

        val result = SimulationConfigOverrider.overrideOptionsFile(config, overrides)

        result shouldBe expected
    }
})
