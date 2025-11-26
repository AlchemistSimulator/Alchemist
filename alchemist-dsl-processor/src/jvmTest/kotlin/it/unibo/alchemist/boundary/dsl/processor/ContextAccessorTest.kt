/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

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
    fun `test exporter context accessors`() {
        assertEquals(
            "ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.EXPORTER_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.scenarioGenerator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.EXPORTER_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.EXPORTER_CONTEXT),
        )
    }

    @Test
    fun `test global programs context accessors`() {
        assertEquals(
            "ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.GLOBAL_PROGRAMS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.scenarioGenerator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.GLOBAL_PROGRAMS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.GLOBAL_PROGRAMS_CONTEXT),
        )
    }

    @Test
    fun `test output monitors context accessors`() {
        assertEquals(
            "ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.OUTPUT_MONITORS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.scenarioGenerator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.OUTPUT_MONITORS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.OUTPUT_MONITORS_CONTEXT),
        )
    }

    @Test
    fun `test terminators context accessors`() {
        assertEquals(
            "ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.TERMINATORS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.scenarioGenerator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.TERMINATORS_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.TERMINATORS_CONTEXT),
        )
    }

    @Test
    fun `test deployment context accessors`() {
        assertEquals(
            "ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.DEPLOYMENT),
        )
        assertEquals("ctx.generator", ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.DEPLOYMENT))
        assertEquals(
            "ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.DEPLOYMENT),
        )
    }

    @Test
    fun `test deployment context singular accessors`() {
        assertEquals(
            "ctx.ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.DEPLOYMENT_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.generator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.DEPLOYMENT_CONTEXT),
        )
        assertEquals(
            "ctx.ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.DEPLOYMENT_CONTEXT),
        )
        assertEquals("ctx.filter", ContextAccessor.getAccessor(InjectionType.FILTER, ContextType.DEPLOYMENT_CONTEXT))
    }

    @Test
    fun `test program context accessors`() {
        assertEquals(
            "ctx.ctx.ctx.ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.PROGRAM),
        )
        assertEquals(
            "ctx.ctx.ctx.ctx.generator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.PROGRAM),
        )
        assertEquals(
            "ctx.ctx.ctx.ctx.ctx.incarnation",
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
        assertEquals(
            "ctx.ctx.ctx.ctx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.PROPERTY),
        )
        assertEquals(
            "ctx.ctx.ctx.ctx.generator",
            ContextAccessor.getAccessor(InjectionType.GENERATOR, ContextType.PROPERTY),
        )
        assertEquals(
            "ctx.ctx.ctx.ctx.ctx.incarnation",
            ContextAccessor.getAccessor(InjectionType.INCARNATION, ContextType.PROPERTY),
        )
        assertEquals("ctx.node", ContextAccessor.getAccessor(InjectionType.NODE, ContextType.PROPERTY))
    }

    @Test
    fun `test custom context parameter name`() {
        assertEquals(
            "customCtx.ctx.environment",
            ContextAccessor.getAccessor(InjectionType.ENVIRONMENT, ContextType.DEPLOYMENT, "customCtx"),
        )
        assertEquals(
            "customCtx.node",
            ContextAccessor.getAccessor(InjectionType.NODE, ContextType.PROGRAM, "customCtx"),
        )
    }
}
