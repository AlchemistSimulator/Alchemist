/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComposeUiDemoScenarioTest {
    @Test
    fun `the demo scenario loads with the compose monitor`() {
        val scenario = assertNotNull(javaClass.classLoader.getResource("composeui-demo.yml"))
        val simulation = LoadAlchemist.from(scenario).getDefault<Any, Euclidean2DPosition>()
        assertEquals(1000, simulation.environment.nodeCount.current)
        assertTrue(simulation.outputMonitors.any { it is ComposeMonitor })
    }
}
