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
 * Represents the lifecycle state of a component.
 */
enum class LifecycleState {

    /**
     * Destroyed state for a LifecycleOwner. After this state is reached, this Lifecycle will not emit any more events.
     */
    DESTROYED,

    /**
     * Initialized state for a LifecycleOwner.
     */
    INITIALIZED,

    /**
     * Started state for a LifecycleOwner.
     */
    STARTED,

    ;

    /**
     * Checks if the current state is at least the given [state].
     */
    fun isAtLeast(state: LifecycleState): Boolean = this >= state
}
