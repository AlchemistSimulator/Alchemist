/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import javafx.event.Event

/**
 * The type of a certain action.
 */
interface ActionType

/**
 * An action listener.
 * @param A the type of actions listened
 * @param T the type of events that trigger the actions
 */
interface ActionListener<A : ActionType, T : Event> {

    /**
     * To be called whenever a certain action happens.
     * @param actionType the type of action that happened
     * @param event the event that triggered the action
     */
    fun onAction(actionType: A, event: T)
}

/**
 * An event dispatcher.
 * @param A the type of the action this dispatcher models
 * @param O the type of a token object containing information regarding the trigger of an action
 * @param T the type of event that triggered the action
 */
abstract class EventDispatcher<A : ActionType, O : Any, T : Event> {

    protected var actions: Map<Pair<A, O>, (event: T) -> Unit> = emptyMap()

    /**
     * The listener bound to this dispatcher.
     */
    abstract val listener: ActionListener<A, T>

    /**
     * Adds an action to be performed whenever an event is triggered through the [listener].
     * @param actionType the type of the action that needs to occur.
     * @param item the item containing information regarding each specific occurrence.
     * @param action the action that will happen whenever the given action occurs.
     */
    open fun setOnAction(actionType: A, item: O, action: (event: T) -> Unit): Unit {
        Pair(actionType, item).let {
            if (it !in actions) {
                actions += it to action
            }
        }
    }
}

/**
 * An event dispatcher which doesn't overwrite its actions when [setOnAction] is called on an already existing trigger.
 */
abstract class PersistentEventDispatcher<A : ActionType, O : Any, T : Event> : EventDispatcher<A, O, T>() {
    override fun setOnAction(actionType: A, item: O, action: (event: T) -> Unit): Unit {
        Pair(actionType, item).let {
            if (it !in actions) {
                super.setOnAction(actionType, item, action)
            }
        }
    }
}