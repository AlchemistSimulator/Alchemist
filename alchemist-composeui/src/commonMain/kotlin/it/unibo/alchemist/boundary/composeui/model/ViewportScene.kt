/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable
import it.unibo.alchemist.boundary.composeui.util.ViewportNodes.toWorldBounds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * State of the central scene area.
 *
 * @property nodes the nodes to draw.
 * @property edges the links to draw.
 * @property edgeCount the total number of links, drawn or not.
 * @property showLinks whether links are shown.
 * @property linkRenderMode how links are drawn.
 * @property linkRenderNotice why links are sampled or hidden, if they are.
 * @property dimensions the number of dimensions of the environment.
 * @property worldBounds the box containing all nodes; `null` if there are none.
 * @property summary the values shown in the summary bar.
 * @property message the text shown when there is nothing to draw.
 */
@Immutable
data class ViewportScene(
    val nodes: ImmutableList<ViewportNode> = persistentListOf(),
    val edges: ImmutableList<ViewportEdge> = persistentListOf(),
    val edgeCount: Int = edges.size,
    val showLinks: Boolean = false,
    val linkRenderMode: LinkRenderMode = LinkRenderMode.FULL,
    val linkRenderNotice: String? = null,
    val dimensions: Int = 2,
    val worldBounds: ViewportWorldBounds? = nodes.toWorldBounds(),
    val summary: ImmutableList<Labeled<Any>> = persistentListOf(),
    val message: String = "Waiting for simulation data",
)
