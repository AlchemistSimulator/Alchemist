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

/**
 * The value of a molecule in one node.
 *
 * @property nodeId the node.
 * @property holds whether the node contains the molecule.
 * @property value the value, `null` if the node does not contain the molecule.
 */
@Immutable
internal data class NodeValue(val nodeId: Int, val holds: Boolean, val value: Any? = null) {
    internal companion object {
        /** The value of [molecule] in [node]. */
        fun of(node: ViewportNode, molecule: String): NodeValue {
            val held = node.concentrations.firstOrNull { it.label == molecule }
            return NodeValue(nodeId = node.id, holds = held != null, value = held?.value)
        }
    }
}
