/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.actions

import io.kvision.redux.RAction
import it.unibo.alchemist.boundary.graphql.client.EnvironmentSubscription

/**
 * Represents actions that can be performed on the environment state.
 * This sealed class defines different types of actions as its subclasses.
 */
sealed class EnvironmentStateAction : RAction {

    /**
     * Action to set the nodes in the environment state.
     * @param nodes The list of nodes to set.
     */
    data class SetNodes(val nodes: MutableList<EnvironmentSubscription.Entry>) : EnvironmentStateAction()

    /**
     * Action to add all nodes to the environment state.
     * @param nodes The list of nodes to add.
     */
    data class AddAllNodes(val nodes: List<EnvironmentSubscription.Entry>) : EnvironmentStateAction()
}
