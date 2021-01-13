/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.jfx.events.mouse

import javafx.scene.input.MouseButton

/**
 * A [MouseTriggerAction] related to mouse button presses.
 *
 * @param type the type of the action performed with the mouse
 * @param button the button related to the action performed
 */
data class MouseButtonTriggerAction(val type: ActionOnMouse, val button: MouseButton) : MouseTriggerAction
