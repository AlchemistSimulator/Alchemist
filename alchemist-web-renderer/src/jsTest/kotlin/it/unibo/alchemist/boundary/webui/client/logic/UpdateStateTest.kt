/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.logic

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.RenderMode

class UpdateStateTest : StringSpec({

    var clientCount = 0
    var serverCount = 0
    var statusCount = 0

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
        override fun invoke(): RenderMode =
            if (clientCount >= serverCount) {
                RenderMode.SERVER
            } else {
                RenderMode.CLIENT
            }
    }

    val brokenAutoStrategy = object : AutoRenderModeStrategy {
        override fun invoke(): RenderMode = RenderMode.AUTO
    }

    "updateState should work using RenderMode.SERVER" {
        updateState(RenderMode.SERVER, updateStateStrategy, autoStrategy)
        statusCount shouldBe 1
        clientCount shouldBe 0
        serverCount shouldBe 1
    }

    "updateState should work using RenderMode.CLIENT" {
        updateState(RenderMode.CLIENT, updateStateStrategy, autoStrategy)
        statusCount shouldBe 2
        clientCount shouldBe 1
        serverCount shouldBe 1
    }

    "updateState should work using RenderMode.AUTO" {
        updateState(RenderMode.AUTO, updateStateStrategy, autoStrategy)
        statusCount shouldBe 3
        clientCount shouldBe 1
        serverCount shouldBe 2
    }

    "updateState should launch Exception using a broken autoStrategy" {
        shouldThrow<IllegalStateException> {
            updateState(RenderMode.AUTO, updateStateStrategy, brokenAutoStrategy)
        }
    }
})
