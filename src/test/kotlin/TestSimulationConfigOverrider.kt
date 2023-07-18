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
            "launcher.type=TestLauncher",
            "launcher.variables=[10, 12, 13]",
            "launcher.parallelism=10",
            "launcher.end-time=100.5",
            "launcher.is-web=false",
            "launcher.is-batch=false",
            "launcher.distributed-configuration-path=test",
            "launcher.graphics-path=test",
            "launcher.server-configuration-path=test",
            "launcher.engine-mode=batchFixed",
            "launcher.epsilon=0.0001",
            "launcher.output-replay-strategy=aggregate",
        )
        val expected = SimulationConfig(
            type = "TestLauncher",
            parameters = SimulationConfig.SimulationConfigParameters(
                variables = listOf("10", "12", "13"),
                parallelism = 10,
                endTime = 100.5,
                isWeb = false,
                isBatch = false,
                distributedConfigPath = "test",
                graphicsPath = "test",
                serverConfigPath = "test",
                engineMode = EngineMode.BATCH_FIXED,
                epsilon = 0.0001,
                outputReplayStrategy = OutputReplayStrategy.AGGREGATE,
            ),
        )

        val result = SimulationConfigOverrider.overrideOptionsFile(config, overrides)

        result shouldBe expected
    }
})
