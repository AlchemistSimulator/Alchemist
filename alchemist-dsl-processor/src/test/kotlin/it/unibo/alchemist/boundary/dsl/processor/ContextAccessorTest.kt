package it.unibo.alchemist.boundary.dsl.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContextAccessorTest {
    @Test
    fun `test simulation context accessors`() {
        assertEquals("ctx.environment", ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.SIMULATION))
        assertEquals(
            "ctx.scenarioGenerator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.SIMULATION),
        )
        assertEquals("ctx.incarnation", ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.SIMULATION))
    }

    @Test
    fun `test deployment context accessors`() {
        assertEquals("ctx.env", ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.DEPLOYMENT))
        assertEquals("ctx.generator", ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.DEPLOYMENT))
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.DEPLOYMENT),
        )
    }

    @Test
    fun `test program context accessors`() {
        assertEquals("ctx.ctx.ctx.env", ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.PROGRAM))
        assertEquals("ctx.ctx.ctx.generator", ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.PROGRAM))
        assertEquals(
            "ctx.ctx.ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.PROGRAM),
        )
        assertEquals("ctx.node", ContextAccessor.getAccessor(InjectionType.NODE, ContextType.PROGRAM))
        assertEquals("ctx.reaction", ContextAccessor.getAccessor(InjectionType.REACTION, ContextType.PROGRAM))
        assertEquals(
            "ctx.timeDistribution!!",
            ContextAccessor.getAccessor(InjectionType.TIMEDISTRIBUTION, ContextType.PROGRAM),
        )
    }

    @Test
    fun `test property context accessors`() {
        assertEquals("ctx.ctx.ctx.env", ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.PROPERTY))
        assertEquals(
            "ctx.ctx.ctx.generator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.PROPERTY),
        )
        assertEquals(
            "ctx.ctx.ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.PROPERTY),
        )
        assertEquals("ctx.node", ContextAccessor.getAccessor(InjectionType.NODE, ContextType.PROPERTY))
    }

    @Test
    fun `test custom context parameter name`() {
        assertEquals(
            "customCtx.env",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.DEPLOYMENT, "customCtx"),
        )
        assertEquals(
            "customCtx.node",
            ContextAccessor.getAccessor(InjectionType.NODE, ContextType.PROGRAM, "customCtx"),
        )
    }
}
