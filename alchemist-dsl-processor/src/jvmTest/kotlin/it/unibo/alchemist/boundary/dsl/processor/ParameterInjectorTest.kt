package it.unibo.alchemist.boundary.dsl.processor

import it.unibo.alchemist.boundary.dsl.processor.data.InjectionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParameterInjectorTest {
    @Test
    fun `test determineContextType with node and reaction`() {
        val injectionIndices = mapOf(
            InjectionType.NODE to 0,
            InjectionType.REACTION to 1,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.PROGRAM_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with only environment`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with only incarnation`() {
        val injectionIndices = mapOf(
            InjectionType.INCARNATION to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with environment and generator`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, contextType)
    }

    @Test
    fun `test getInjectionParams with all enabled`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
            InjectionType.NODE to 2,
        )
        val annotationValues = emptyMap<String, Any?>()

        val paramsToSkip = ParameterInjector.getInjectionParams(
            injectionIndices,
            annotationValues,
            ContextType.PROGRAM_CONTEXT,
        )

        assertEquals(setOf(0, 1, 2), paramsToSkip)
    }

    @Test
    fun `test getInjectionParams with environment disabled`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
        )
        val annotationValues = mapOf("injectEnvironment" to false)

        val paramsToSkip = ParameterInjector.getInjectionParams(
            injectionIndices,
            annotationValues,
            ContextType.DEPLOYMENTS_CONTEXT,
        )

        assertEquals(setOf(1), paramsToSkip)
    }

    @Test
    fun `test getInjectionParams with time distribution always injected`() {
        val injectionIndices = mapOf(
            InjectionType.TIMEDISTRIBUTION to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val paramsToSkip = ParameterInjector.getInjectionParams(
            injectionIndices,
            annotationValues,
            ContextType.PROGRAM_CONTEXT,
        )

        assertEquals(setOf(0), paramsToSkip)
    }

    @Test
    fun `test getInjectionParams with GlobalProgramsContext only injects available parameters`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.TIMEDISTRIBUTION to 1,
        )
        val annotationValues = emptyMap<String, Any?>()

        val paramsToSkip = ParameterInjector.getInjectionParams(
            injectionIndices,
            annotationValues,
            ContextType.GLOBAL_PROGRAMS_CONTEXT,
        )

        assertEquals(setOf(0), paramsToSkip)
    }

    @Test
    fun `test determineContextType with manual scope PROGRAM override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "PROGRAM")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.PROGRAM_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope SIMULATION override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
        )
        val annotationValues = mapOf("scope" to "SIMULATION")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope DEPLOYMENT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "DEPLOYMENT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope DEPLOYMENTS_CONTEXT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "DEPLOYMENTS_CONTEXT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope PROPERTY override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "PROPERTY")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.PROPERTY_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with filter parameter`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.FILTER to 1,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.DEPLOYMENT_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with filter and manual scope override`() {
        val injectionIndices = mapOf(
            InjectionType.FILTER to 0,
        )
        val annotationValues = mapOf("scope" to "PROGRAM")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.PROGRAM_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope EXPORTER_CONTEXT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "EXPORTER_CONTEXT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.EXPORTER_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope GLOBAL_PROGRAMS_CONTEXT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "GLOBAL_PROGRAMS_CONTEXT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.GLOBAL_PROGRAMS_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope OUTPUT_MONITORS_CONTEXT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "OUTPUT_MONITORS_CONTEXT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.OUTPUT_MONITORS_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with manual scope TERMINATORS_CONTEXT override`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "TERMINATORS_CONTEXT")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.TERMINATORS_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with invalid scope falls back to automatic detection`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "INVALID_SCOPE")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION_CONTEXT, contextType)
    }

    @Test
    fun `test determineContextType with empty scope falls back to automatic detection`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = mapOf("scope" to "")

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION_CONTEXT, contextType)
    }

    @Test
    fun `test parseScope with valid values`() {
        assertEquals(ContextType.PROGRAM_CONTEXT, ParameterInjector.parseScope("PROGRAM"))
        assertEquals(ContextType.SIMULATION_CONTEXT, ParameterInjector.parseScope("SIMULATION"))
        assertEquals(ContextType.SIMULATION_CONTEXT, ParameterInjector.parseScope("SIMULATION_CONTEXT"))
        assertEquals(ContextType.EXPORTER_CONTEXT, ParameterInjector.parseScope("EXPORTER"))
        assertEquals(ContextType.EXPORTER_CONTEXT, ParameterInjector.parseScope("EXPORTER_CONTEXT"))
        assertEquals(ContextType.GLOBAL_PROGRAMS_CONTEXT, ParameterInjector.parseScope("GLOBAL_PROGRAMS"))
        assertEquals(ContextType.GLOBAL_PROGRAMS_CONTEXT, ParameterInjector.parseScope("GLOBAL_PROGRAMS_CONTEXT"))
        assertEquals(ContextType.OUTPUT_MONITORS_CONTEXT, ParameterInjector.parseScope("OUTPUT_MONITORS"))
        assertEquals(ContextType.OUTPUT_MONITORS_CONTEXT, ParameterInjector.parseScope("OUTPUT_MONITORS_CONTEXT"))
        assertEquals(ContextType.TERMINATORS_CONTEXT, ParameterInjector.parseScope("TERMINATORS"))
        assertEquals(ContextType.TERMINATORS_CONTEXT, ParameterInjector.parseScope("TERMINATORS_CONTEXT"))
        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, ParameterInjector.parseScope("DEPLOYMENT"))
        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, ParameterInjector.parseScope("DEPLOYMENTS_CONTEXT"))
        assertEquals(ContextType.DEPLOYMENT_CONTEXT, ParameterInjector.parseScope("DEPLOYMENT_CONTEXT"))
        assertEquals(ContextType.PROPERTY_CONTEXT, ParameterInjector.parseScope("PROPERTY"))
        assertEquals(ContextType.PROPERTY_CONTEXT, ParameterInjector.parseScope("PROPERTY_CONTEXT"))
        assertEquals(ContextType.PROGRAM_CONTEXT, ParameterInjector.parseScope("PROGRAM_CONTEXT"))
        assertEquals(ContextType.PROGRAM_CONTEXT, ParameterInjector.parseScope("program"))
        assertEquals(ContextType.SIMULATION_CONTEXT, ParameterInjector.parseScope("simulation"))
        assertEquals(ContextType.DEPLOYMENTS_CONTEXT, ParameterInjector.parseScope("deployments_context"))
        assertEquals(ContextType.EXPORTER_CONTEXT, ParameterInjector.parseScope("exporter"))
    }

    @Test
    fun `test parseScope with invalid values`() {
        assertEquals(null, ParameterInjector.parseScope("INVALID"))
        assertEquals(null, ParameterInjector.parseScope(""))
        assertEquals(null, ParameterInjector.parseScope(null))
    }
}
