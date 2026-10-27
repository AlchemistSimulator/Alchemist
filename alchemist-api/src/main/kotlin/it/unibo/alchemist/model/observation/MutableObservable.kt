/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

/**
 * A MutableObservable represents an extension of the Observable interface, designed to maintain
 * mutable state and notify its observers when the state changes, which happens by setting [current].
 *
 * @param T The type of the value being observed and modified.
 */
interface MutableObservable<T> : Observable<T> {
    override var current: T
}
