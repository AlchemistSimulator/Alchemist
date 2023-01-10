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
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.TestUtility.webRendererTestEnvironments
import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.server.monitor.EnvironmentMonitorFactory.makeEnvironmentMonitor
import it.unibo.alchemist.server.state.ServerStore
import it.unibo.alchemist.server.state.actions.SetEnvironmentSurrogate

class EnvironmentMonitorTest : StringSpec({
    fun checkStep(action: () -> Unit) {
        val initialEnvironment = ServerStore.store.state.environmentSurrogate
        action()
        initialEnvironment shouldNotBe ServerStore.store.state.environmentSurrogate
        ServerStore.store.dispatch(SetEnvironmentSurrogate(EnvironmentSurrogate.uninitializedEnvironment()))
    }

    "EnvironmentMonitor stepDone should work as expected" {
        webRendererTestEnvironments<Any, Nothing>().forEach {
            val environmentMonitor = makeEnvironmentMonitor(it)
            checkStep {
                environmentMonitor.stepDone(it, null, Time.ZERO, 111)
            }
            checkStep {
                environmentMonitor.initialized(it)
            }
            checkStep {
                environmentMonitor.finished(it, Time.ZERO, 222)
            }
        }
    }
})
