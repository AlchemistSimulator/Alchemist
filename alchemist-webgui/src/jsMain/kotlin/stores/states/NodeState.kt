/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.states

import it.unibo.alchemist.boundary.graphql.client.NodeQuery

/**
 * Represents the state of a node, including its data retrieved from a NodeQuery.
 * @property node The node data retrieved from a NodeQuery, or null if the node is not available.
 * @constructor Creates a NodeState with the specified node data, which defaults to null.
 */
data class NodeState(val node: NodeQuery.Data? = null)
