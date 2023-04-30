/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.dispatchers

import it.unibo.alchemist.boundary.fxui.interaction.EventDispatcher
import it.unibo.alchemist.boundary.fxui.interaction.TriggerAction
import javafx.event.Event

/**
 * A generic event dispatcher that implements action management.
 */
abstract class AbstractEventDispatcher<T : TriggerAction, E : Event> : EventDispatcher<T, E> {

    /**
     * Inheriting classes can access and modify the collection of triggers to change the behaviour of the dispatcher.
     */
    protected var triggers: Map<T, (event: E) -> Unit> = emptyMap()

    override fun set(trigger: T, job: (event: E) -> Unit) {
        triggers += trigger to job
    }
}
