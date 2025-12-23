/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.observation.Observable
import java.io.Serializable

/**
 * A time-distributed generic reaction which reactively reacts to changes
 * to its inbound dependencies by emitting through [rescheduleRequest].
 */
sealed interface ReactiveReaction<T> :
    Comparable<ReactiveReaction<T>>,
    Serializable,
    Disposable {

    /**
     * Emits when this reaction requests the scheduler to reschedule
     * this reaction.
     */
    val rescheduleRequest: Observable<Unit>

    /**
     * @return The [Node] in which this [ReactiveReaction] executes.
     */
    val node: ObservableNode<T>

    /**
     *  The list of [ReactiveAction]s of the [Observable].
     *  Please be careful when you modify this list.
     */
    var actions: List<ReactiveAction<T>>

    /**
     * The list of [ReactiveCondition]s of the [Observable].
     * Please be careful when you modify this list.
     */
    var conditions: List<ReactiveCondition<T>>

    /**
     * Returns the speed of this [ReactiveReaction]. It is an average number, and
     * can potentially change during the simulation, depending on the
     * implementation.
     *
     * @return the number of times this [ReactiveReaction] is triggered per time
     * unit.
     */
    val rate: Double

    /**
     * @return The global [Time] at which this reaction is scheduled to be
     * executed
     */
    val tau: Time get() = timeDistribution.nextOccurence

    /**
     * @return the [TimeDistribution] for this [ReactiveReaction]
     */
    val timeDistribution: TimeDistribution<T>

    /**
     * @return true if the reaction can be executed (namely, all the conditions
     * are satisfied).
     */
    fun canExecute(): Boolean

    /**
     * Executes the reactions.
     */
    fun execute()

    /**
     * This method is called when the environment has completed its
     * initialization. Can be used by this reaction to compute its next
     * execution time - in case such computation requires an inspection of the
     * environment.
     *
     * @param atTime the time at which the initialization of this reaction was accomplished.
     * @param environment the environment.
     */
    fun initializationComplete(atTime: Time, environment: ObservableEnvironment<T, *>)

    /**
     * Updates the scheduling of this reaction.
     *
     * @param hasBeenExecuted true if the reaction have just been executed.
     * @param currentTime the current [Time] of execution. This is mandatory in
     *                    order to correctly compute the time shift of an
     *                    already-scheduled reaction.
     * @param environment the current environment.
     */
    fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: ObservableEnvironment<T, *>)

    /**
     * This method allows to clone this reaction on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     *
     * @param node
     * The node where to clone this Reaction
     * @param currentTime
     * the time at which the clone is created (required to correctly clone the [TimeDistribution]s)
     * @return the cloned action
     */
    fun cloneOnNewNode(node: ObservableNode<T>, currentTime: Time): ReactiveReaction<T>
}
