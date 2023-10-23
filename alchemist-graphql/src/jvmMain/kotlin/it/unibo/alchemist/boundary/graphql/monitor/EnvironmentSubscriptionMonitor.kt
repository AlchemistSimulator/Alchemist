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
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * An [OutputMonitor] that emits a new [Environment] as a [EnvironmentSurrogate]
 * each time the [OutputMonitor.stepDone] function is called.
 *
 * @param T the concentration type
 * @param P the position
 */
class EnvironmentSubscriptionMonitor<T, P : Position<out P>> : OutputMonitor<T, P> {
    private val internalFlow = MutableSharedFlow<EnvironmentSurrogate<T, P>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Returns a [Flow] that emits a new [EnvironmentSurrogate] each time the
     * [OutputMonitor.stepDone] function is called.
     */
    val eventFlow: Flow<EnvironmentSurrogate<T, P>> get() = internalFlow

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        internalFlow.tryEmit(environment.toGraphQLEnvironmentSurrogate())
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        this.stepDone(environment, null, time, step)
    }

    override fun initialized(environment: Environment<T, P>) {
        this.stepDone(environment, null, Time.ZERO, 0L)
    }
}
