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
 * The [TriggerAction] objects regarding key strokes.
 */
enum class ActionOnKey {
    PRESSED,
    RELEASED;

    /**
     * Returns a [KeyboardTriggerAction] initialized with this [ActionOnKey] and the given [KeyCode].
     *
     * @param [key] the [KeyCode] used to initialize the [KeyboardTriggerAction].
     */
    infix fun with(key: KeyCode): KeyboardTriggerAction = KeyboardTriggerAction(this, key)
}

/**
 * The keyboard-related [TriggerAction].
 *
 * @param [type] the action performed with the key.
 * @param [key] the on which the action is performed.
 */
data class KeyboardTriggerAction(val type: ActionOnKey, val key: KeyCode) : TriggerAction

/**
 * An action listener in the context of a keyboard.
 */
interface KeyboardActionListener : ActionListener<KeyboardTriggerAction, KeyEvent>

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
