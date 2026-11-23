/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

/**
 * The callbacks triggered by the inspector.
 *
 * @property onDismiss closes the inspector.
 * @property onConcentrationSet sets a molecule to a value, both as typed, in the given nodes.
 * @property onMoleculeRemoved removes a molecule from the given nodes.
 * @property onValueEditorToggled opens a molecule of the given nodes in the large value editor, or closes it.
 */
internal class InspectorActions(
    val onDismiss: () -> Unit = {},

    val onConcentrationSet: (nodeIds: List<Int>, molecule: String, value: String) -> Unit = { _, _, _ -> },

    val onMoleculeRemoved: (nodeIds: List<Int>, molecule: String) -> Unit = { _, _ -> },

    val onValueEditorToggled: (nodeIds: List<Int>, molecule: String) -> Unit = { _, _ -> },
)
