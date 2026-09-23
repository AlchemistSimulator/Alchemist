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

/** Base implementation of a reactive [Condition]. */
open class AbstractCondition<T>(private val node: Node<T>) : Condition<T> {
    private var validity: Observable<Boolean> = MutableObservable.observe(true)

    override fun getNode(): Node<T> = node

    override fun isValid(): Observable<Boolean> = validity

    override fun dispose() {
        validity.dispose()
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): Condition<T> =
        throw UnsupportedOperationException("${javaClass.simpleName} has no support for cloning.")

    /**
     * Installs the observable backing [isValid].
     *
     * The condition owns the derived view created here, while [newValidity] remains owned by its model source.
     */
    protected fun setValidity(newValidity: Observable<Boolean>) {
        validity.dispose()
        validity = newValidity.map { it }
    }

    override fun toString(): String = javaClass.simpleName
}
