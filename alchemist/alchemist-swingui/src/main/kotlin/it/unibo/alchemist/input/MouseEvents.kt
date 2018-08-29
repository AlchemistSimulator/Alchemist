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

/**
 * The [ActionType] objects regarding mouse clicks
 */
enum class MouseButtonActionType : ActionType {
    CLICKED,
    DRAGGED,
    ENTERED,
    EXITED,
    MOVED,
    PRESSED,
    RELEASED
}

/**
 * An action listener in the context of mouse button events.
 */
interface MouseButtonActionListener : ActionListener<MouseButtonActionType, MouseEvent>

/**
 * A basic implementation of a mouse button event dispatcher.
 */
open class SimpleMouseButtonEventDispatcher : AbstractEventDispatcher<MouseButtonActionType, MouseButton, MouseEvent>() {
    override val listener: ActionListener<MouseButtonActionType, MouseEvent> = object : MouseButtonActionListener {
        override fun onAction(actionType: MouseButtonActionType, event: MouseEvent) {
            actions[Pair(actionType, event.button)]?.invoke(event)
        }
    }
}