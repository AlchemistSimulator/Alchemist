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
 * An event dispatcher.
 * @param T the type of the action this dispatcher models
 * @param E the type of event that triggers this dispatcher
 */
interface EventDispatcher<in T : TriggerAction, E : Event> {

    /**
     * The listener bound to this dispatcher.
     */
    val listener: ActionListener<T, E>

    /**
     * Adds a job to be performed whenever an event triggers the dispatcher through the [listener].
     * @param trigger the type of the job that needs to occur.
     * @param job the job that will happen whenever the given job occurs.
     */
    operator fun set(trigger: T, job: (event: E) -> Unit): Unit
}
