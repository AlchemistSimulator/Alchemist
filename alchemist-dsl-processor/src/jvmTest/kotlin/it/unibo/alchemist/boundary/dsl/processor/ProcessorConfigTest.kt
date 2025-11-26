package it.unibo.alchemist.boundary.dsl.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProcessorConfigTest {
    @Test
    fun `test environment package patterns`() {
        assertTrue(ProcessorConfig.isEnvironmentPackage("it.unibo.alchemist.model.Environment"))
        assertTrue(ProcessorConfig.isEnvironmentPackage("it.unibo.alchemist.model.maps.OSMEnvironment"))
        assertTrue(
            ProcessorConfig.isEnvironmentPackage("it.unibo.alchemist.model.environments.Continuous2DEnvironment"),
        )
        assertTrue(!ProcessorConfig.isEnvironmentPackage("com.example.Environment"))
    }

    @Test
    fun `test config constants`() {
        assertEquals("it.unibo.alchemist.model", ProcessorConfig.MODEL_PACKAGE)
        assertEquals("it.unibo.alchemist.boundary.dsl", ProcessorConfig.DSL_PACKAGE)
        assertEquals("it.unibo.alchemist.boundary.dsl.generated", ProcessorConfig.GENERATED_PACKAGE)
        assertEquals("it.unibo.alchemist.model.Position", ProcessorConfig.POSITION_TYPE)
        assertEquals("it.unibo.alchemist.model.Environment", ProcessorConfig.ENVIRONMENT_TYPE)
    }
}
