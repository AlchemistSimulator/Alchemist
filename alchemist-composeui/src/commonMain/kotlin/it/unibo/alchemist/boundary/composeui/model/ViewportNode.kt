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
import it.unibo.alchemist.boundary.composeui.model.InspectorState.SingleNode
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * A node projected in the central viewport.
 *
 * @property id the node id.
 * @property coordinates the node position; only the first two are drawn.
 * @property metadata extra details about the node.
 * @property concentrations the node contents, by molecule.
 * @property reactions the reactions as text; `null` unless the node is inspected, as they are costly to build.
 */
@Immutable
internal data class ViewportNode(
    val id: Int,
    val coordinates: ImmutableList<Double>,
    val metadata: ImmutableList<Labeled<Any?>> = persistentListOf(),
    val concentrations: ImmutableList<Labeled<Any?>> = persistentListOf(),
    val reactions: ImmutableList<String>? = null,
) {
    init {
        require(coordinates.size >= 2) {
            "Viewport nodes require at least two coordinates."
        }
    }

    /** The inspector view of this node. */
    internal fun toInspectorState(): SingleNode = SingleNode(
        nodeId = id,
        subtitle = "Live node snapshot",
        position = persistentListOf(Labeled("X", coordinates[0]), Labeled("Y", coordinates[1])),
        concentrations = concentrations,
        metadata = metadata,
        reactions = reactions,
    )

    /** The current position, to send back to the simulation. */
    internal fun toPositionUpdate(): NodePositionUpdate = NodePositionUpdate(id, coordinates)

    /** This node moved by [deltaX] and [deltaY] on the plane. */
    internal fun translate(deltaX: Double, deltaY: Double): ViewportNode = copy(
        coordinates = coordinates.mapIndexed { index, coordinate ->
            coordinate + when (index) {
                0 -> deltaX
                1 -> deltaY
                else -> 0.0
            }
        }.toImmutableList(),
    )

    /** This node with [molecule] at [value], in place if it was there already, or last otherwise. */
    internal fun withConcentration(molecule: String, value: Any?): ViewportNode {
        val updated = Labeled(molecule, value)
        val contents = when {
            concentrations.any { it.label == molecule } ->
                concentrations.map { if (it.label == molecule) updated else it }
            else -> concentrations + updated
        }
        return copy(concentrations = contents.toImmutableList())
    }

    /** This node at [newCoordinates]. */
    internal fun withCoordinates(newCoordinates: List<Double>): ViewportNode =
        copy(coordinates = newCoordinates.toImmutableList())

    /** This node without [molecule]. */
    internal fun withoutMolecule(molecule: String): ViewportNode =
        copy(concentrations = concentrations.filterNot { it.label == molecule }.toImmutableList())
}
