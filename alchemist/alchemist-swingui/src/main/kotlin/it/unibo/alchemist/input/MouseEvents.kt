/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import javafx.scene.Node
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
    private var temporaryActions: Map<MouseTriggerAction, (MouseEvent) -> Unit> = emptyMap()

    override val listener = object : MouseActionListener {
        override fun action(action: MouseTriggerAction, event: MouseEvent) {
            if (action in temporaryActions.keys) {
                temporaryActions[action]?.apply {
                    invoke(event)
                    temporaryActions -= action
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
 * A mouse event dispatcher that catches mouse input from a node.
 */
open class NodeBoundMouseEventDispatcher(node: Node) : TemporariesMouseEventDispatcher() {
    init {
        node.setOnMouseClicked {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.CLICKED, it.button), it)
        }
        node.setOnMouseDragged {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.DRAGGED, it.button), it)
        }
        node.setOnMouseEntered {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.ENTERED, it.button), it)
        }
        node.setOnMouseExited {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.EXITED, it.button), it)
        }
        node.setOnMouseMoved {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.MOVED, it.button), it)
        }
        node.setOnMousePressed {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.PRESSED, it.button), it)
        }
        node.setOnMouseReleased {
            listener.action(MouseButtonTriggerAction(ActionOnMouse.RELEASED, it.button), it)
        }
        node.setOnMouseMoved {
            listener.action(BasicMouseTriggerAction.MOVEMENT, it)
        }
    }
}
