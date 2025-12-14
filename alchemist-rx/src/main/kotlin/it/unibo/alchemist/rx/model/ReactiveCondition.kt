/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableSet

/**
 * A reactive [Condition][it.unibo.alchemist.model.Condition] which emits
 * validity and propensity contribution when changes to its dependencies
 * are detected.
 */
interface ReactiveCondition<T> {

    /**
     * Emits when this condition's dependencies change. Emits true
     * if this condition is valid, false otherwise.
     */
    val isValid: Observable<Boolean>

    /**
     * Emits when this condition's dependencies change. Emits
     * the propensity contribution as a [Double] typically
     * based on the validity of this condition.
     */
    val propensityContribution: Observable<Double>

    /**
     * An [ObservableSet] of dependencies as [Observable]s, useful for determine
     * which are the inbound dependencies of this [ReactiveCondition] and the [reaction][ReactiveReaction]
     * that hosts this condition.
     */
    val observableInboundDependencies: ObservableSet<Observable<*>>

    /**
     * TODO: implement me somehow
     */
    fun cloneCondition(node: ObservableNode<T>, reaction: ReactiveReaction<T>): ReactiveCondition<T> =
        throw UnsupportedOperationException("${this::class.simpleName} has no support for cloning.")

    /**
     * Set of simple handy extension functions.
     */
    companion object {

        /**
         * Propagate validity and propensity changes through a unique [Observable] representing
         * validity and the propensity contribution as a tuple.
         *
         * @return validity and propensity contribution as a [Pair].
         */
        fun <T> ReactiveCondition<T>.validityToPropensity(): Observable<Pair<Boolean, Double>> =
            isValid.mergeWith(propensityContribution) { valid, propensity -> valid to propensity }
    }
}
