/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * A basic implementation of [KeyboardEventDispatcher].
 */
open class SimpleKeyboardEventDispatcher : KeyboardEventDispatcher() {

    private var keysHeld: Set<KeyCode> = emptySet()

    override val listener = object : KeyboardActionListener {
        override fun action(action: KeyboardTriggerAction, event: KeyEvent) {
            triggers[action]?.invoke(event)
            keysHeld = when (action.type) {
                ActionOnKey.PRESSED -> keysHeld + action.key
                ActionOnKey.RELEASED -> keysHeld - action.key
            }
            event.consume()
        }
    }

    override fun isHeld(key: KeyCode) = key in keysHeld
}
