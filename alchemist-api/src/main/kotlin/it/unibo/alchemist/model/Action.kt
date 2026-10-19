/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * An operation performed when its owning [Reaction] executes.
 *
 * Actions are imperative: [execute] reads the model state it needs, such as neighborhoods or concentrations,
 * when it runs. Actions should not subscribe to observables
 * (e.g., through [Observable.onChange][it.unibo.alchemist.model.observation.Observable.onChange])
 * to keep a copy of that state: nothing releases such subscriptions when the reaction or its node is removed,
 * so they outlive the action and may run against nodes that are no longer in the environment;
 * moreover, the copy goes stale whenever the state it derives from changes without the observed value changing.
 * Reactive dependencies belong to [Condition]s, whose validity is owned by their reaction.
 *
 * @param T concentration type
 */
interface Action<T> {
    /**
     * The reaction owning this action.
     * Its [Reaction.host] is the model entity hosting the action, which may differ from the node it targets, if any.
     */
    val reaction: Reaction<T>

    /**
     * Creates an equivalent action owned by [newReaction], such as a reaction cloned onto a new node.
     * An action targeting the host of its reaction targets the host of [newReaction], which must then be a [Node];
     * an action targeting any other node keeps its target.
     */
    fun cloneAction(newReaction: Reaction<T>): Action<T>

    /**
     * Applies this action to the model.
     */
    fun execute()
}
