/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.monitor

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.graphql.util.EnvironmentEventBus
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time

/**
 * A monitor that emits the environment through a [kotlinx.coroutines.flow.MutableSharedFlow].
 * @param environmentEmitter the environment emitter as a [kotlinx.coroutines.flow.MutableSharedFlow]
 */
class EnvironmentSubscriptionMonitor<T, P : Position<out P>>(
    private val environmentEmitter: EnvironmentEventBus<T, P>,
) : OutputMonitor<T, P> {
    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        sendEnvironment(environment)
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        this.stepDone(environment, null, time, step)
    }

    override fun initialized(environment: Environment<T, P>) {
        this.stepDone(environment, null, Time.ZERO, 0L)
    }

    private fun sendEnvironment(environment: Environment<T, P>) {
        environmentEmitter.sendEnvironment(environment)
    }
}
