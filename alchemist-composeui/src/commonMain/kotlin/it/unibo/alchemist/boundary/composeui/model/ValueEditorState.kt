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
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.typeName
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A molecule open in the large value editor, with its live value in each edited node.
 *
 * @property molecule the molecule.
 * @property values its value in each edited node.
 */
@Immutable
internal data class ValueEditorState(val molecule: String, val values: ImmutableList<NodeValue>) {
    /** The edited nodes. */
    val nodeIds: ImmutableList<Int> = values.map(NodeValue::nodeId).toImmutableList()

    /** Which nodes are edited. */
    val subtitle: String = values.singleOrNull()?.let { "Node ${it.nodeId}" } ?: "${values.size} nodes"

    /** The value shared by every node, as text, or empty if nodes differ or lack the molecule. */
    val sharedText: String = values
        .takeIf { all -> all.all(NodeValue::holds) }
        ?.map(NodeValue::value)
        ?.distinct()
        ?.singleOrNull()
        ?.toString()
        .orEmpty()

    /** The type of the values, and in how many nodes the molecule is missing. */
    val typeText: String = run {
        val held = values.filter(NodeValue::holds)
        val types = held.map { it.value.typeName() }.distinct().joinToString(", ")
        val absent = values.size - held.size
        when {
            held.isEmpty() -> "absent"
            absent == 0 -> types
            else -> "$types; absent in $absent of ${values.size} nodes"
        }
    }

    /** The full value, or the value in each node if they differ. */
    val valueText: String = when {
        values.size == 1 || sharedText.isNotEmpty() -> values.first().describe()
        else -> values.joinToString("\n") { "Node ${it.nodeId}: ${it.describe()}" }
    }

    internal companion object {
        /** The editor for [molecule] in those of [nodeIds] still in [scene], or `null` if none is left. */
        fun of(scene: ViewportScene, nodeIds: Collection<Int>, molecule: String): ValueEditorState? {
            val wanted = nodeIds.toSet()
            return scene.nodes
                .filter { it.id in wanted }
                .map { NodeValue.of(it, molecule) }
                .takeIf { it.isNotEmpty() }
                ?.let { ValueEditorState(molecule, it.toImmutableList()) }
        }

        private fun NodeValue.describe(): String = if (holds) value.toString() else "absent"
    }
}
