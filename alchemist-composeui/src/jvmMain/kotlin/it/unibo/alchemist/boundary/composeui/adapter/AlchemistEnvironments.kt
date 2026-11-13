/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.adapter.AlchemistNodes.toViewport
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlinx.collections.immutable.toImmutableList

/**
 * Turns Alchemist environments into what the viewport draws.
 */
internal object AlchemistEnvironments {
    /** This environment as the viewport shows it; links are collected only if [renderLinks]. */
    fun <T, P : Position<P>> Environment<T, P>.toViewport(renderLinks: Boolean = false): ViewportScene {
        val links = if (renderLinks) EdgeSnapshot.of(neighborPairs()) else EdgeSnapshot()
        return ViewportScene(
            nodes = nodes.current.map { it.toViewport(this) }.toImmutableList(),
            edges = links.edges,
            edgeCount = links.edgeCount,
            linkRenderMode = links.renderMode,
            linkRenderNotice = links.notice,
            dimensions = dimensions,
        )
    }

    private fun <T, P : Position<P>> Environment<T, P>.neighborPairs(): Sequence<Pair<Int, Int>> =
        nodes.current.asSequence().flatMap { node ->
            neighborhoodOf(node).current.neighbors.asSequence().map { neighbor -> node.id to neighbor.id }
        }
}
