/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableMutableSet
import it.unibo.alchemist.model.observation.ObservableSet

/** Base implementation of a reactive [Condition]. */
open class AbstractCondition<T>(private val node: Node<T>) : Condition<T> {

    private val dependencies = ObservableMutableSet<Observable<*>>()

    private var validity: Observable<Boolean> = MutableObservable.observe(true)

    override fun getNode(): Node<T> = node

    /** Records [dependency] as a model value whose changes may affect this condition. */
    protected fun addObservableDependency(dependency: Observable<*>): Observable<*> = dependency.also(dependencies::add)

    final override fun getDependencies(): ObservableSet<out Observable<*>> = dependencies.copy()

    override fun isValid(): Observable<Boolean> = validity

    override fun dispose() {
        validity.dispose()
        dependencies.dispose()
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): Condition<T> =
        throw UnsupportedOperationException("${javaClass.simpleName} has no support for cloning.")

    /** Installs the observable backing [isValid]. */
    protected fun setValidity(newValidity: Observable<Boolean>) {
        validity = newValidity
    }

    override fun toString(): String = javaClass.simpleName
}
