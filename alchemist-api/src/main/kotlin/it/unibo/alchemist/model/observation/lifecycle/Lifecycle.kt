/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation.lifecycle

/**
 * Manages the state and listeners.
 */
interface Lifecycle {

    /**
     * Returns the current state of the Lifecycle.
     */
    val currentState: LifecycleState

    /**
     * Adds a LifecycleObserver that will be notified when the LifecycleOwner changes state.
     */
    fun addObserver(observer: (LifecycleState) -> Unit)

    /**
     * Removes the given observer from the observers list.
     */
    fun removeObserver(observer: (LifecycleState) -> Unit)
}
