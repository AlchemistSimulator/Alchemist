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
 * The action that triggers an event dispatcher. May contain information about the trigger.
 */
interface TriggerAction

/**
 * An action listener.
 * @param T the type of triggers
 * @param E the type of events that trigger the triggers
 */
interface ActionListener<T : TriggerAction, E : Event> {

    /**
     * To be called whenever a certain action happens.
     * @param action the action that happened
     * @param event the event that triggered the action
     */
    fun action(action: T, event: E)
}

/**
 * An event dispatcher.
 * @param T the type of the action this dispatcher models
 * @param E the type of event that triggers this dispatcher
 */
interface EventDispatcher<T : TriggerAction, E : Event> {

    /**
     * The listener bound to this dispatcher.
     */
    val listener: ActionListener<T, E>

    /**
     * Adds a job to be performed whenever an event triggers the dispatcher through the [listener].
     * @param trigger the type of the job that needs to occur.
     * @param job the job that will happen whenever the given job occurs.
     */
    operator fun set(trigger: T, job: (event: E) -> Unit): Unit
}

/**
 * A generic event dispatcher that implements action management.
 */
abstract class AbstractEventDispatcher<T : TriggerAction, E : Event> : EventDispatcher<T, E> {

    protected var triggers: Map<T, (event: E) -> Unit> = emptyMap()

    override fun set(trigger: T, job: (event: E) -> Unit) {
        triggers += trigger to job
    }
}

/**
 * An event dispatcher which doesn't overwrite its triggers when [set] is called on an already existing trigger.
 */
abstract class PersistentEventDispatcher<T : TriggerAction, E : Event> : AbstractEventDispatcher<T, E>() {

    override fun set(trigger: T, job: (event: E) -> Unit) {
        if (trigger !in triggers) {
            super.set(trigger, job)
        }
    }
}