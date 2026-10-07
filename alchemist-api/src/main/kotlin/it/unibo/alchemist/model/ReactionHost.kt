/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.observation.ObservableList

/**
 * A model entity owning [Reaction] membership.
 *
 * Implementations notify the running simulation after membership actually changes. Consequently, callers only
 * register or unregister reactions through their host: scheduler synchronization is not their responsibility.
 */
interface ReactionHost<T> {
    /** The reactions registered with this host, in registration order; [ObservableList.current] is a snapshot. */
    val reactions: ObservableList<Reaction<T>>

    /**
     * Registers [reaction] and notifies the running simulation when it was not already present.
     * Fails if this is not the [Reaction.host] of [reaction].
     */
    fun addReaction(reaction: Reaction<T>)

    /** Unregisters [reaction] and notifies the running simulation when it was actually present. */
    fun removeReaction(reaction: Reaction<T>)
}
