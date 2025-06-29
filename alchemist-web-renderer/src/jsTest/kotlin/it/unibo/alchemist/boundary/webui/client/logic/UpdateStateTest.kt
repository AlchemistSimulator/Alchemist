/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.logic

import it.unibo.alchemist.boundary.webui.common.model.RenderMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class UpdateStateTest {

    private var clientCount = 0
    private var serverCount = 0
    private var statusCount = 0

    val updateStateStrategy = object : UpdateStateStrategy {
        override suspend fun clientComputation() {
            clientCount++
        }

        override suspend fun serverComputation() {
            serverCount++
        }

        override suspend fun retrieveSimulationStatus() {
            statusCount++
        }
    }

    val autoStrategy = object : AutoRenderModeStrategy {
        override fun invoke(): RenderMode = when {
            clientCount >= serverCount -> RenderMode.SERVER
            else -> RenderMode.CLIENT
        }
    }

    private val brokenAutoStrategy = object : AutoRenderModeStrategy {
        override fun invoke(): RenderMode = RenderMode.AUTO
    }

    @Test
    fun `updateState should work using all render modes`() = runTest {
        updateState(RenderMode.SERVER, updateStateStrategy, autoStrategy)
        assertEquals(1, statusCount, "retrieveSimulationStatus should be called once")
        assertEquals(0, clientCount, "clientComputation should not be called")
        assertEquals(1, serverCount, "serverComputation should be called once")
        updateState(RenderMode.CLIENT, updateStateStrategy, autoStrategy)
        assertEquals(2, statusCount, "retrieveSimulationStatus count should accumulate")
        assertEquals(1, clientCount, "clientComputation should be called once")
        assertEquals(1, serverCount, "serverComputation count should remain")
        updateState(RenderMode.AUTO, updateStateStrategy, autoStrategy)
        assertEquals(3, statusCount, "retrieveSimulationStatus count should accumulate")
        assertEquals(1, clientCount, "clientComputation count should remain")
        assertEquals(2, serverCount, "serverComputation should be called again")
        assertFailsWith<IllegalStateException> {
            updateState(RenderMode.AUTO, updateStateStrategy, brokenAutoStrategy)
        }
    }
}
