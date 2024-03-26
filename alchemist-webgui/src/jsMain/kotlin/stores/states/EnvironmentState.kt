/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.states

import it.unibo.alchemist.boundary.graphql.client.EnvironmentSubscription

/**
 * Represents the state of the environment, including a list of nodes.
 * @property nodes The list of nodes in the environment.
 * @constructor Creates an EnvironmentState with the specified list of nodes.
 */
data class EnvironmentState(val nodes: MutableList<EnvironmentSubscription.Entry>) {

    /**
     * Converts the list of nodes to a list of pairs representing their coordinates.
     * @return A list of pairs where each pair contains the x and y coordinates of a node.
     */
    fun toListOfPairs(): List<Pair<Double, Double>> {
        return nodes.map { e -> Pair(e.position.coordinates[0], e.position.coordinates[1]) }
    }
}
