/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * The [ActionType] objects regarding key strokes.
 */
enum class KeyActionType : ActionType {
    PRESSED,
    RELEASED
}

/**
 * An action listener in the context of a keyboard.
 */
interface KeyboardActionListener : ActionListener<KeyActionType, KeyEvent>

/**
 * An action dispatcher in the context of a keyboard.
 */
abstract class KeyboardEventDispatcher : PersistentEventDispatcher<KeyActionType, KeyCode, KeyEvent>() {

    /**
     * Returns whether a given key is being held or not at the time of the call.
     * @param key the queried key
     * @returns whether the given key is being held or not
     */
    abstract fun isHeld(key: KeyCode): Boolean
}

/**
 * A basic implementation of [KeyboardEventDispatcher]
 */
open class SimpleKeyboardEventDispatcher : KeyboardEventDispatcher() {

    private var keyActions: Map<Pair<KeyActionType, KeyCode>, (event: KeyEvent) -> Unit> = emptyMap()
    private var keysHeld: Set<KeyCode> = emptySet()

    override val listener = object : KeyboardActionListener {
        override fun onAction(actionType: KeyActionType, event: KeyEvent) {
            when (actionType) {
                KeyActionType.PRESSED -> {
                    event.code.let {
                        keyActions[Pair(KeyActionType.PRESSED, event.code)]?.invoke(event)
                        keysHeld += it
                    }
                }
                KeyActionType.RELEASED -> {
                    event.code.let {
                        keyActions[Pair(KeyActionType.RELEASED, event.code)]?.invoke(event)
                        keysHeld -= it
                    }
                }
            }
            event.consume()
        }
    }

    override fun isHeld(key: KeyCode) = key in keysHeld
}