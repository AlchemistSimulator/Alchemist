/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

enum class ActionOnMouse {
    CLICKED,
    DRAGGED,
    ENTERED,
    EXITED,
    MOVED,
    PRESSED,
    RELEASED
}

enum class MouseCommand {
    PAN,
    SELECT,
    REMOVE,
    MOVE
}

data class MouseButtonTriggerAction(val type: ActionOnMouse, val button: MouseButton, val command: MouseCommand) : TriggerAction

/**
 * An action listener in the context of mouse button events.
 */
interface MouseButtonActionListener : ActionListener<MouseButtonTriggerAction, MouseEvent>

/**
 * A basic implementation of a mouse button event dispatcher.
 */
open class SimpleMouseButtonEventDispatcher : AbstractEventDispatcher<MouseButtonTriggerAction, MouseEvent>() {
    override val listener: ActionListener<MouseButtonTriggerAction, MouseEvent> = object : MouseButtonActionListener {
        override fun onAction(action: MouseButtonTriggerAction, event: MouseEvent) {
            triggers[action]?.invoke(event)
        }
    }
}