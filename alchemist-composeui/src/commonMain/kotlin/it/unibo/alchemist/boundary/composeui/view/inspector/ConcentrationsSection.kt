/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.NodeValue
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.toDisplayString
import it.unibo.alchemist.boundary.composeui.view.components.TransportButtonSize
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.controls.DockTextField
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private const val OPEN_GLYPH = "…"

/** Value boxes take this many times the room of the molecule names, and grow with the inspector. */
private const val VALUE_WEIGHT = 2f

private val rowSpacing = 8.dp
private val nodeRowIndent = 16.dp

/**
 * An inspector card listing the [concentrations] of the nodes [nodeIds], whose values can be edited.
 * Enter sets a changed value; × removes a molecule; the last row adds a molecule, or sets it if already there.
 * A molecule with [valuesByNode] can be expanded to show and edit its value in each node.
 * … opens a molecule in the large editor, for long values, and closes it if it is already open.
 * Values are sent as typed through [actions]: the simulation parses them.
 */
@Composable
internal fun ConcentrationsSection(
    title: String,
    description: String,
    nodeIds: List<Int>,
    concentrations: List<Labeled<Any?>>,
    actions: InspectorActions,
    valuesByNode: Map<String, List<NodeValue>> = emptyMap(),
) {
    InspectorCard(title, description) {
        concentrations.forEach { field ->
            key(field.label) {
                ConcentrationRow(field, nodeIds, valuesByNode[field.label].orEmpty(), actions)
            }
            Divider(color = Outline.copy(alpha = DIVIDER_ALPHA))
        }
        NewMoleculeRow { molecule, value -> actions.onConcentrationSet(nodeIds, molecule, value) }
    }
}

@Composable
private fun ConcentrationRow(
    field: Labeled<Any?>,
    nodeIds: List<Int>,
    perNode: List<NodeValue>,
    actions: InspectorActions,
) {
    val molecule = field.label
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(rowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphButton(glyph = OPEN_GLYPH, description = "Open $molecule in the large editor") {
                actions.onValueEditorToggled(nodeIds, molecule)
            }
            if (perNode.isNotEmpty()) {
                GlyphButton(
                    glyph = if (expanded) "▾" else "▸",
                    description = if (expanded) "Hide $molecule in each node" else "Show $molecule in each node",
                    onClick = { expanded = !expanded },
                )
            }
            Text(
                text = molecule,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2,
                color = TextSecondary,
            )
            ValueField(shown = field.value.toDisplayString(), modifier = Modifier.weight(VALUE_WEIGHT)) {
                actions.onConcentrationSet(nodeIds, molecule, it)
            }
            SquareButton(text = "×", description = "Remove $molecule") { actions.onMoleculeRemoved(nodeIds, molecule) }
        }
        if (expanded) {
            perNode.forEach { nodeValue ->
                key(nodeValue.nodeId) {
                    NodeValueRow(molecule, nodeValue, actions)
                }
            }
        }
    }
}

@Composable
private fun NodeValueRow(molecule: String, nodeValue: NodeValue, actions: InspectorActions) {
    val node = listOf(nodeValue.nodeId)
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = nodeRowIndent),
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphButton(
            glyph = OPEN_GLYPH,
            description = "Open $molecule of node ${nodeValue.nodeId} in the large editor",
            onClick = { actions.onValueEditorToggled(node, molecule) },
        )
        Text(
            text = "Node ${nodeValue.nodeId}" + if (nodeValue.holds) "" else ", absent",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.caption,
            color = TextSecondary,
        )
        ValueField(
            shown = if (nodeValue.holds) nodeValue.value.toDisplayString() else "",
            description = "$molecule in node ${nodeValue.nodeId}",
            modifier = Modifier.weight(VALUE_WEIGHT),
        ) { actions.onConcentrationSet(node, molecule, it) }
        if (nodeValue.holds) {
            SquareButton(text = "×", description = "Remove $molecule from node ${nodeValue.nodeId}") {
                actions.onMoleculeRemoved(node, molecule)
            }
        } else {
            Spacer(modifier = Modifier.size(TransportButtonSize))
        }
    }
}

/** A box showing [shown] that sends the typed value to [onSet] on Enter, unless unchanged. */
@Composable
private fun ValueField(shown: String, modifier: Modifier, description: String? = null, onSet: (String) -> Unit) {
    var draft by remember { mutableStateOf("") }
    DockTextField(
        label = null,
        value = draft,
        error = null,
        onValueChange = { draft = it },
        // Unchanged values, such as a group's "Mixed", are not sent.
        onSubmit = { draft.takeIf { it != shown }?.let(onSet) },
        width = Dp.Unspecified,
        default = shown,
        modifier = description?.let { modifier.semantics { contentDescription = it } } ?: modifier,
    )
}

/** A narrow button showing [glyph]. */
@Composable
private fun GlyphButton(glyph: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(TransportButtonSize / 2, TransportButtonSize)
            .semantics { contentDescription = description }
            .clickableKeepingShortcuts(onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, style = MaterialTheme.typography.subtitle1, color = SecondaryAccent)
    }
}

@Composable
private fun NewMoleculeRow(onSet: (molecule: String, value: String) -> Unit) {
    var molecule by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    val add = {
        molecule.trim().takeIf { it.isNotEmpty() }?.let {
            onSet(it, value)
            molecule = ""
            value = ""
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockTextField(
            label = "new",
            value = molecule,
            error = null,
            onValueChange = { molecule = it },
            onSubmit = { add() },
            width = Dp.Unspecified,
            modifier = Modifier.weight(1f),
        )
        DockTextField(
            label = null,
            value = value,
            error = null,
            onValueChange = { value = it },
            onSubmit = { add() },
            width = Dp.Unspecified,
            modifier = Modifier.weight(VALUE_WEIGHT),
        )
        SquareButton(text = "+", description = "Add the molecule", onClick = { add() })
    }
}

@Composable
private fun SquareButton(text: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(TransportButtonSize)
            .clip(componentShape)
            .border(width = 1.dp, color = Outline, shape = componentShape)
            .semantics { contentDescription = description }
            .clickableKeepingShortcuts(onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.subtitle1, color = SecondaryAccent)
    }
}
