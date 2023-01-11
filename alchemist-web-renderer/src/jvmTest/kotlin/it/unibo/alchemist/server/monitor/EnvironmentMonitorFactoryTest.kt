/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.monitor

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.TestUtility.webRendererTestEnvironments
import it.unibo.alchemist.server.monitor.EnvironmentMonitorFactory.makeEnvironmentMonitor

class EnvironmentMonitorFactoryTest : StringSpec({
    "EnvironmentMonitorFactory should create a monitor using different strategy" {
        webRendererTestEnvironments<Any, Nothing>().forEach {
            makeEnvironmentMonitor(it).let { monitor ->
                monitor::class shouldBe EnvironmentMonitor::class
            }
        }
    }
})
