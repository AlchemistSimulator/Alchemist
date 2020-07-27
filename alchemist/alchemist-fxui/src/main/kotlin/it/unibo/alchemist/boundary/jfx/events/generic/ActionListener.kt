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
 * An action listener.
 * @param T the type of triggers
 * @param E the type of events that trigger the triggers
 */
interface ActionListener<T : TriggerAction, E : Event> {

    /**
     * To be called whenever a certain action happens.
     * @param action the action that happened
     * @param event the event that triggered the action
     */
    fun action(action: T, event: E)
}
