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
 * A concrete implementation of [Lifecycle] that handles state transitions and observer notification.
 */
class LifecycleRegistry : Lifecycle {

    private val observers = mutableListOf<(LifecycleState) -> Unit>()

    override var currentState: LifecycleState = LifecycleState.INITIALIZED
        private set

    /**
     * Transitions the lifecycle to a new [state] and notifies all observers.
     */
    fun markState(state: LifecycleState) {
        currentState = state
        observers.toList().forEach { it(state) }
    }

    override fun addObserver(observer: (LifecycleState) -> Unit) {
        observers.add(observer)
    }

    override fun removeObserver(observer: (LifecycleState) -> Unit) {
        observers.remove(observer)
    }
}
