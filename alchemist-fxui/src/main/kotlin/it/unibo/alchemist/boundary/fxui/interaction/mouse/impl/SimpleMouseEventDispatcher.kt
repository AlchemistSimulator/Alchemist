/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.mouse.impl

import it.unibo.alchemist.boundary.fxui.interaction.mouse.api.MouseActionListener
import it.unibo.alchemist.boundary.fxui.interaction.mouse.api.MouseTriggerAction
import javafx.scene.input.MouseEvent

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
