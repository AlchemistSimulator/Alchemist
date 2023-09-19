/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.NeighborhoodSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.model.Neighborhood

fun <T> checkNeighborhood(n: Neighborhood<T>, ns: NeighborhoodSurrogate<T>) {
    n.size() shouldBe ns.size
    n.isEmpty shouldBe ns.isEmpty()
    n.center.toGraphQLNodeSurrogate() shouldBe ns.getCenter()

    val node = n.neighbors.first()
    ns.contains(node.toGraphQLNodeSurrogate()) shouldBe true
    ns.getNeighbors().shouldContainAll(n.neighbors.map { it.toGraphQLNodeSurrogate() })
}
