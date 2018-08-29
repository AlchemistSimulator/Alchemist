/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * The action on the key.
 */
enum class KeyAction {
    PRESSED,
    RELEASED
}

/**
 * An action listener for keyboard input on a JavaFX node.
 */
interface KeyboardActionListener {

    /**
     * To be called when a key is being pressed.
     * @param event the key being pressed
     */
    fun keyPressed(event: KeyEvent)

    /**
     * To be called when a key has been released.
     * @param event the key being released
     */
    fun keyReleased(event: KeyEvent)
}

/**
 * An event dispatcher for keyboard input on a JavaFX node.
 */
interface KeyboardEventDispatcher {

    /**
     * The listener bound to this dispatcher.
     */
    val listener: KeyboardActionListener

    /**
     * Adds an action to be performed whenever a given key is being pressed.
     * @param key the key being pressed
     * @param action the action that will be performed
     */
    fun setOnKeyPressed(key: KeyCode, action: (event: KeyEvent) -> Unit): Unit

    /**
     * Adds an action to be performed whenever a given key has been released.
     * @param key the released key
     * @param action the action that will be performed
     */
    fun setOnKeyReleased(key: KeyCode, action: (event: KeyEvent) -> Unit): Unit

    /**
     * Returns whether a given key is being held or not at the time of the call.
     * @param key the queried key
     * @return whether the given key is being held or not
     */
    fun isHeld(key: KeyCode): Boolean
}

/**
 * A basic implementation of the [KeyboardEventDispatcher]
 */
class SimpleKeyboardEventDispatcher : KeyboardEventDispatcher {

    private var keyActions: Map<Pair<KeyCode, KeyAction>, (event: KeyEvent) -> Unit> = emptyMap()
    private var keysHeld: Set<KeyCode> = emptySet()

    override val listener = object : KeyboardActionListener {

        override fun keyPressed(event: KeyEvent) {
            event.code.let {
                keyActions[Pair(it, KeyAction.PRESSED)]?.invoke(event)
                keysHeld += it
            }
        }

        override fun keyReleased(event: KeyEvent) {
            event.code.let {
                keyActions[Pair(it, KeyAction.RELEASED)]?.invoke(event)
                keysHeld -= it
            }
        }
    }

    override fun setOnKeyPressed(key: KeyCode, action: (event: KeyEvent) -> Unit) {
        if (key !in keyActions.keys.map { it.first }) {
            keyActions += Pair(key, KeyAction.PRESSED) to action
        }
    }

    override fun setOnKeyReleased(key: KeyCode, action: (event: KeyEvent) -> Unit) {
        if (key !in keyActions.keys.map { it.first }) {
            keyActions += Pair(key, KeyAction.RELEASED) to action
        }
    }

    override fun isHeld(key: KeyCode) = key in keysHeld
}
