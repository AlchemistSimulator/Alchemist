/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

/**
 * Actions that can happen on a mouse and a certain mouse button.
 */
enum class ActionOnMouse {
    CLICKED,
    DRAGGED,
    ENTERED,
    EXITED,
    MOVED,
    PRESSED,
    RELEASED
}

data class MouseTriggerAction(val type: ActionOnMouse, val button: MouseButton) : TriggerAction

/**
 * An action listener in the context of mouse events.
 */
interface MouseActionListener : ActionListener<MouseTriggerAction, MouseEvent>

/**
 * A basic implementation of a mouse event dispatcher.
 */
open class SimpleMouseEventDispatcher : AbstractEventDispatcher<MouseTriggerAction, MouseEvent>() {
    override val listener: ActionListener<MouseTriggerAction, MouseEvent> = object : MouseActionListener {
        override fun action(action: MouseTriggerAction, event: MouseEvent) {
            triggers[action]?.invoke(event)
        }
    }
}

/**
 * A mouse event dispatcher that catches mouse input from a canvas.
 */
open class CanvasBoundMouseEventDispatcher(canvas: Canvas) : SimpleMouseEventDispatcher() {
    init {
        canvas.setOnMouseClicked {
            listener.action(MouseTriggerAction(ActionOnMouse.CLICKED, it.button), it)
        }
        canvas.setOnMouseDragged {
            listener.action(MouseTriggerAction(ActionOnMouse.DRAGGED, it.button), it)
        }
        canvas.setOnMouseEntered {
            listener.action(MouseTriggerAction(ActionOnMouse.ENTERED, it.button), it)
        }
        canvas.setOnMouseExited {
            listener.action(MouseTriggerAction(ActionOnMouse.EXITED, it.button), it)
        }
        canvas.setOnMouseMoved {
            listener.action(MouseTriggerAction(ActionOnMouse.MOVED, it.button), it)
        }
        canvas.setOnMousePressed {
            listener.action(MouseTriggerAction(ActionOnMouse.PRESSED, it.button), it)
        }
        canvas.setOnMouseReleased {
            listener.action(MouseTriggerAction(ActionOnMouse.RELEASED, it.button), it)
        }
    }
}