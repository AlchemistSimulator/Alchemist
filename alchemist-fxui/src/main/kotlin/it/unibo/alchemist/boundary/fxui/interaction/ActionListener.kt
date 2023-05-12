/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction

import javafx.event.Event

/**
 * An action listener.
 * @param T the type of triggers
 * @param E the type of events that trigger the triggers
 */
interface ActionListener<in T : TriggerAction, in E : Event> {

    /**
     * To be called whenever a certain action happens.
     * @param action the action that happened
     * @param event the event that triggered the action
     */
    fun action(action: T, event: E)
}
