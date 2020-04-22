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
 * The enum's values are based on JavaFX's mouse events, such as onMouseClicked
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

/**
 * The [TriggerAction] regarding mouse events.
 */
interface MouseTriggerAction : TriggerAction

/**
 * Simple mouse actions.
 */
enum class BasicMouseTriggerAction : MouseTriggerAction {
    MOVEMENT
}

/**
 * A [MouseTriggerAction] related to mouse button presses.
 *
 * @param type the type of the action performed with the mouse
 * @param button the button related to the action performed
 */
data class MouseButtonTriggerAction(val type: ActionOnMouse, val button: MouseButton) : MouseTriggerAction

/**
 * An action listener in the context of mouse events.
 */
interface MouseActionListener : ActionListener<MouseTriggerAction, MouseEvent>

/**
 * An event dispatcher in the context of mouse events.
 */
abstract class MouseEventDispatcher : PersistentEventDispatcher<MouseTriggerAction, MouseEvent>() {

    abstract override val listener: MouseActionListener
}

/**
 * A basic implementation of a mouse event dispatcher.
 */
open class SimpleMouseEventDispatcher : MouseEventDispatcher() {

    override val listener = object : MouseActionListener {
        override fun action(action: MouseTriggerAction, event: MouseEvent) {
            triggers[action]?.invoke(event)
        }
    }
}

/**
 * A mouse event dispatcher which can receive temporary actions to listen to which will only be triggered once.
 * These temporary actions have a higher priority than actions set through [setOnAction].
 */
open class TemporariesMouseEventDispatcher : SimpleMouseEventDispatcher() {
    private var temporaryActions: List<Pair<MouseTriggerAction, (MouseEvent) -> Unit>> = emptyList()

    override val listener = object : MouseActionListener {
        override fun action(action: MouseTriggerAction, event: MouseEvent) {
            if (action in temporaryActions.map { it.first }) {
                temporaryActions.find { it.first == action }?.let {
                    it.second.invoke(event)
                    temporaryActions -= it
                }
            } else {
                triggers[action]?.invoke(event)
            }
        }
    }

    /**
     * Set a temporary action.
     * @param trigger the trigger for the action
     * @param job the job that will be run when the action occurs
     */
    fun setOnActionTemporary(trigger: MouseTriggerAction, job: (MouseEvent) -> Unit) {
        temporaryActions += trigger to job
    }
}

/**
 * A mouse event dispatcher that catches mouse input from a canvas.
 */
open class CanvasBoundMouseEventDispatcher(canvas: Canvas) : TemporariesMouseEventDispatcher() {
    init {
        canvas.setOnMouseClicked {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.CLICKED, it.button), it)
        }
        canvas.setOnMouseDragged {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.DRAGGED, it.button), it)
        }
        canvas.setOnMouseEntered {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.ENTERED, it.button), it)
        }
        canvas.setOnMouseExited {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.EXITED, it.button), it)
        }
        canvas.setOnMouseMoved {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.MOVED, it.button), it)
        }
        canvas.setOnMousePressed {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.PRESSED, it.button), it)
        }
        canvas.setOnMouseReleased {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.RELEASED, it.button), it)
        }
        canvas.setOnMouseMoved {
            listener.action(BasicMouseTriggerAction.MOVEMENT, it)
        }
    }
}
