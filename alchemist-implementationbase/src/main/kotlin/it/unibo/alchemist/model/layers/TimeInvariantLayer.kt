/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observation.Observable

/**
 * A [Layer] whose spatial distribution never changes over time: the value observed at a position is fixed, so
 * observers of a node's layer value are notified only when the node moves.
 *
 * Implementations define the distribution through [currentValueAt].
 *
 * @param T the type of the value measuring the substance at a position
 * @param P the position type
 */
abstract class TimeInvariantLayer<T, P : Position<out P>> : Layer<T, P> {
    abstract override fun currentValueAt(position: P): T

    // The value at a position never changes: its observers are never notified, except on registration.
    final override fun valueAt(position: P): Observable<T> = object : AbstractObservable<T>() {
        private val value = currentValueAt(position)

        override fun computeFresh(): T = value

        override fun toString(): String = "Constant($value)"
    }
}
