package it.unibo.alchemist.boundary.dsl.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        assertEquals(ContextType.PROGRAM, contextType)
    }

    @Test
    fun `test determineContextType with only environment`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION, contextType)
    }

    @Test
    fun `test determineContextType with only incarnation`() {
        val injectionIndices = mapOf(
            InjectionType.INCARNATION to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.SIMULATION, contextType)
    }

    @Test
    fun `test determineContextType with environment and generator`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
        )
        val annotationValues = emptyMap<String, Any?>()

        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)

        assertEquals(ContextType.DEPLOYMENT, contextType)
    }

    @Test
    fun `test getInjectionParams with all enabled`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
            InjectionType.NODE to 2,
        )
        val annotationValues = emptyMap<String, Any?>()

        val paramsToSkip = ParameterInjector.getInjectionParams(injectionIndices, annotationValues)

        assertEquals(setOf(0, 1, 2), paramsToSkip)
    }

    @Test
    fun `test getInjectionParams with environment disabled`() {
        val injectionIndices = mapOf(
            InjectionType.ENVIRONMENT to 0,
            InjectionType.GENERATOR to 1,
        )
        val annotationValues = mapOf("injectEnvironment" to false)

        val paramsToSkip = ParameterInjector.getInjectionParams(injectionIndices, annotationValues)

        assertEquals(setOf(1), paramsToSkip)
    }

    @Test
    fun `test getInjectionParams with time distribution always injected`() {
        val injectionIndices = mapOf(
            InjectionType.TIMEDISTRIBUTION to 0,
        )
        val annotationValues = emptyMap<String, Any?>()

        val paramsToSkip = ParameterInjector.getInjectionParams(injectionIndices, annotationValues)

        assertEquals(setOf(0), paramsToSkip)
    }
}
