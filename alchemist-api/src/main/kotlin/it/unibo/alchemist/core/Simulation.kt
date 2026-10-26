/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import arrow.core.Option
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observation.Observable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * A simulation, running as an independent thread and controllable from an external console.
 *
 * @param T concentration type
 * @param P position type
 */
interface Simulation<T, P : Position<out P>> : Runnable {
    /**
     * The environment of this simulation. It is not a copy, but the actual environment used in the simulation:
     * manipulate it carefully.
     */
    val environment: Environment<T, P>

    /**
     * The exception that made the simulation fail,
     * or [arrow.core.None] if the simulation is ongoing or has terminated successfully.
     */
    val error: Option<Throwable>

    /**
     * The [OutputMonitor]s currently attached to this simulation.
     */
    val outputMonitors: List<OutputMonitor<T, P>>

    /**
     * The current [SimulationState] of the simulation.
     */
    val state: SimulationState

    /**
     * The current simulation step.
     */
    val step: Long

    /**
     * The simulation time, notifying its observers on the simulation thread whenever it advances.
     */
    val time: Observable<Time>

    /**
     * The current simulation time.
     */
    val currentTime: Time get() = time.current

    /**
     * Adds an [OutputMonitor] to this simulation.
     */
    fun addOutputMonitor(op: OutputMonitor<T, P>)

    /**
     * Executes the simulation up to [steps] steps, then pauses it.
     * The returned future completes when the steps are executed.
     */
    fun goToStep(steps: Long): CompletableFuture<Unit>

    /**
     * Executes the simulation until the target time [time] is reached, then pauses it.
     * The returned future completes when the target time is reached.
     */
    fun goToTime(time: Time): CompletableFuture<Unit>

    /**
     * Sends a pause command to the simulation. There is no guarantee on when this command will be actually processed.
     * The returned future completes when the simulation is paused.
     */
    fun pause(): CompletableFuture<Unit>

    /**
     * Sends a play command to the simulation. There is no guarantee on when this command will be actually processed.
     * The returned future completes when the simulation is played.
     */
    fun play(): CompletableFuture<Unit>

    /**
     * Notifies the simulation that a reaction host has registered [reactionToAdd].
     * Model code should register the reaction through its host; the host invokes this callback after membership
     * actually changes. Adding a node invokes this callback once for each reaction already hosted by that node;
     * node and topology changes have no separate simulation callback.
     */
    fun reactionAdded(reactionToAdd: Reaction<T>)

    /**
     * Marks [reactionToUpdate] as a reaction whose observed scheduling inputs changed.
     * The notification is synchronous and must originate on the simulation thread. The engine refreshes each
     * affected reaction once when the enclosing model mutation completes.
     */
    fun reactionInvalidated(reactionToUpdate: Reaction<T>)

    /**
     * Notifies the simulation that a reaction host has unregistered [reactionToRemove].
     * Model code should unregister the reaction through its host; the host invokes this callback after membership
     * actually changes. Removing a node invokes this callback once for each hosted reaction before disposing it.
     */
    fun reactionRemoved(reactionToRemove: Reaction<T>)

    /**
     * Removes an [OutputMonitor] from this simulation. If [monitor] was not among those already added, nothing happens.
     */
    fun removeOutputMonitor(monitor: OutputMonitor<T, P>)

    /**
     * Schedules [action] to be executed by the simulation thread, useful for synchronization purposes
     * (e.g., make sure that the environment is not being changed while the requested operation is being executed).
     * An exception thrown by [action] makes the simulation terminate.
     */
    fun schedule(action: () -> Unit)

    /**
     * Sends a terminate command to the simulation.
     * There is no guarantee on when this command will be actually processed.
     * The returned future completes when the simulation is terminated.
     */
    fun terminate(): CompletableFuture<Unit>

    /**
     * Suspends the caller until the simulation reaches the selected [SimulationState] [state], or the [timeout],
     * expressed in [timeunit], expires. Waiting for a status does not mean that every [OutputMonitor]
     * has already been notified of the update.
     *
     * @return the status of the simulation at the end of the wait
     */
    fun waitFor(state: SimulationState, timeout: Long, timeunit: TimeUnit): SimulationState
}
