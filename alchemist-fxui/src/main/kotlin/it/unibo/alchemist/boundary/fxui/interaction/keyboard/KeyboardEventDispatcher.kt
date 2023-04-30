/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

import it.unibo.alchemist.boundary.fxui.interaction.dispatchers.PersistentEventDispatcher
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * An event dispatcher in the context of a keyboard.
 */
abstract class KeyboardEventDispatcher : PersistentEventDispatcher<KeyboardTriggerAction, KeyEvent>() {

    abstract override val listener: KeyboardActionListener

    /**
     * Returns whether a given key is being held or not at the time of the call.
     * @param key the queried key
     * @returns whether the given key is being held or not
     */
    abstract fun isHeld(key: KeyCode): Boolean
}
