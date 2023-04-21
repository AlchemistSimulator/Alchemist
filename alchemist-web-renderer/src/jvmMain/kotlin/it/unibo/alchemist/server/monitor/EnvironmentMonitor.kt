/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.monitor

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.server.state.ServerStore.store
import it.unibo.alchemist.server.state.actions.SetEnvironmentSurrogate
import it.unibo.alchemist.server.surrogates.utility.toEnvironmentSurrogate

/**
 * A monitor that maps an [Environment] into an [EnvironmentSurrogate] and saves it in the [store].
 *
 *  @param <P> position type.
 *  @param <T> concentration type.
 *  @param <PS> position surrogate type.
 *  @param <TS> concentration surrogate type.
 *  @param toConcentrationSurrogate the mapping function from <T> to <TS>.
 *  @param toPositionSurrogate the mapping function from <P> to <PS>.
 */
class EnvironmentMonitor<T, P, TS, PS> (
    private val toConcentrationSurrogate: (T) -> TS,
    private val toPositionSurrogate: (P) -> PS,
) : OutputMonitor<T, P>
    where TS : Any, P : Position<P>, PS : PositionSurrogate {

    /**
     * Every time the [Environment] changes, map it to [EnvironmentSurrogate] class and save it in the [store].
     * @param environment the updated environment.
     * @param reaction the reaction that triggered the update.
     * @param time the current time.
     * @param step the current step.
     */
    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        val newEnvironmentSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> =
            environment.toEnvironmentSurrogate(toConcentrationSurrogate, toPositionSurrogate)
        store.dispatch(SetEnvironmentSurrogate(newEnvironmentSurrogate))
    }

    /**
     * Call the stepDone(Environment<T, P>, Actionable<T>?, Time, Long) method setting time and step to 0.
     * @param environment the environment.
     */
    override fun initialized(environment: Environment<T, P>) {
        this.stepDone(environment, null, Time.ZERO, 0L)
    }

    /**
     * Call the stepDone(Environment<T, P>, Actionable<T>?, Time, Long) method setting the reaction to null.
     * @param environment the updated environment.
     * @param time the final time.
     * @param step the final step.
     */
    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        this.stepDone(environment, null, time, step)
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
