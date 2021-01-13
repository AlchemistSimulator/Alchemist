/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.jfx.events.generic

import javafx.event.Event

/**
 * An event dispatcher which doesn't overwrite its triggers when [set] is called on an already existing trigger.
 */
abstract class PersistentEventDispatcher<T : TriggerAction, E : Event> : AbstractEventDispatcher<T, E>() {

    override fun set(trigger: T, job: (event: E) -> Unit) {
        if (trigger !in triggers) {
            super.set(trigger, job)
        }
    }
}
