/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet
import it.unibo.alchemist.rx.model.observation.ObservableSet

/**
 * A [it.unibo.alchemist.model.Condition] which is dependent on some
 * [Observable] elements of the model. The evaluation of this condition
 * is lazy, meaning that its [validity][isValid] and [propensity][propensityContribution]
 * will be marked as stale if any dependency changes, and their values are recomputed
 * only when the value is pulled. If the value is already present and this condition
 * is not marked as stale, a cached value is yielded.
 */
interface ReactiveLazyCondition : Disposable {
    /**
     * True if this condition is valid, false otherwise.
     */
    val isValid: Boolean

    /**
     * The propensity value of this condition which can influence the reaction
     * rate.
     */
    val propensityContribution: Double

    /**
     * An [ObservableSet] of dependencies as [Observable]s, useful for determine
     * which are the inbound dependencies of this condition and the [reaction][ReactiveReaction]
     * that hosts this condition.
     */
    val observableInboundDependencies: ObservableSet<Observable<*>>
}

/**
 * A simple base for defining lazy reactive conditions.
 */
abstract class AbstractReactiveLazyCondition : ReactiveLazyCondition {

    private val deps = ObservableMutableSet<Observable<*>>().apply {
        onChange(this@AbstractReactiveLazyCondition) { markDirty() }
    }

    private var _isValid: LazyValue<Boolean> = LazyValue(false, ::computeValidity)
    override val isValid: Boolean get() = _isValid.current

    private var _propensityContribution: LazyValue<Double> = LazyValue(0.0, ::computePropensity)
    override val propensityContribution: Double get() = _propensityContribution.current

    override val observableInboundDependencies: ObservableSet<Observable<*>> get() = deps

    /**
     * Adds the given [Observable] to the list of sources of changes
     * for this condition - namely its dependencies. If the given
     * dependency is already in this condition's set of dependencies,
     * nothing is done.
     *
     * @param dep the dependency to add to the list of dependencies of this condition.
     */
    protected fun declareDependencyOn(dep: Observable<*>) {
        if (dep in deps) return
        deps.add(dep)
        dep.onChange(this) { markDirty() }
    }

    /**
     * Actual implementation-specific logic for computing the
     * validity of this condition.
     */
    protected abstract fun computeValidity(): Boolean

    /**
     * Actual implementation-specific logic for computing the
     * propensity of this condition.
     */
    protected abstract fun computePropensity(): Double

    override fun dispose() {
        deps.toSet().forEach { it.stopWatching(this) }
        deps.stopWatching(this)
        deps.dispose()
    }

    private fun markDirty() {
        _isValid.markDirty()
        _propensityContribution.markDirty()
    }

    private class LazyValue<T>(initial: T, private val compute: () -> T) {
        private var isDirty: Boolean = false

        private var _current: T = initial

        val current: T
            get() {
                if (isDirty) {
                    _current = compute()
                    isDirty = false
                }
                return _current
            }

        fun markDirty() {
            isDirty = true
        }
    }
}
