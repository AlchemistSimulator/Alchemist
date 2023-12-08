/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.mouse

import javafx.scene.input.MouseEvent

/**
 * A mouse event dispatcher which can receive temporary actions to listen to which will only be triggered once.
 * These temporary actions have higher priority than other actions.
 */
open class DynamicMouseEventDispatcher : SimpleMouseEventDispatcher() {
    private var dynamicActions: Map<MouseTriggerAction, (MouseEvent) -> Unit> = emptyMap()

    override val listener = object : MouseActionListener {
        override fun action(action: MouseTriggerAction, event: MouseEvent) {
            if (action in dynamicActions.keys) {
                dynamicActions[action]?.apply {
                    invoke(event)
                    dynamicActions = dynamicActions - action
                }
            } else {
                triggers[action]?.invoke(event)
            }
        }
    }

    /**
     * Set a dynamic action.
     * @param trigger the trigger for the action
     * @param job the job that will be run when the action occurs
     */
    fun setDynamicAction(trigger: MouseTriggerAction, job: (MouseEvent) -> Unit) {
        dynamicActions = dynamicActions + (trigger to job)
    }
}
