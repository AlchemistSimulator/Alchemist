/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.mouse

import javafx.scene.Node

/**
 * A mouse event dispatcher that catches mouse input from a node.
 */
open class NodeBoundMouseEventDispatcher(node: Node) : DynamicMouseEventDispatcher() {
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
            listener.action(MouseMovement, it)
        }
    }
}
