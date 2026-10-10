/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.observation.Observable

/**
 * A substance or a molecule with a spatial distribution whose values may change over time,
 * associated with an [Environment] during setup.
 *
 * @param T the type of the value measuring the substance at a position
 * @param P the position type
 */
fun interface Layer<T, P : Position<out P>> {
    /**
     * Observes the value at [position]; the returned observable emits every change of that value.
     *
     * The returned observable is lazy: it consumes no resources until subscribed. Subscribers own the returned
     * subscription handles and dispose them when they no longer need updates; they must not dispose the observable
     * itself.
     */
    fun observeValue(position: P): Observable<T>

    /**
     * The current value at [position].
     */
    fun getValue(position: P): T = observeValue(position).current
}
