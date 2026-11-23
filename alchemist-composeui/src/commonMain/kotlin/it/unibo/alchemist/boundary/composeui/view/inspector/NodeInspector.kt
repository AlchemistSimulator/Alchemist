/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import it.unibo.alchemist.boundary.composeui.model.InspectorState.NodeGroup
import it.unibo.alchemist.boundary.composeui.model.InspectorState.SingleNode
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatIdRanges
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

/** How much smaller the text of the inspector is than in the rest of the UI. */
private const val TEXT_SCALE = 0.85f

@Composable
internal fun NodeInspector(inspector: InspectorState, actions: InspectorActions, modifier: Modifier = Modifier) {
    // The inspector holds many small fields: smaller text fits more of them, boxes and buttons included.
    MaterialTheme(typography = MaterialTheme.typography.scaled(TEXT_SCALE)) {
        InspectorPanel(inspector, actions, modifier)
    }
}

@Composable
private fun InspectorPanel(inspector: InspectorState, actions: InspectorActions, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = FloatingPanel,
        contentColor = TextPrimary,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            InspectorHeader(
                title = inspector.title,
                subtitle = inspector.subtitle,
                onDismiss = actions.onDismiss,
            )
            when (inspector) {
                is SingleNode -> SingleNodeInspector(inspector, actions)
                is NodeGroup -> GroupNodeInspector(inspector, actions)
            }
        }
    }
}

@Composable
private fun InspectorHeader(title: String, subtitle: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2,
                color = TextSecondary,
            )
        }
        TransportButton(
            label = "Close",
            enabled = true,
            accent = Outline,
            onClick = onDismiss,
        )
    }
}

@Composable
private fun SingleNodeInspector(inspector: SingleNode, actions: InspectorActions) {
    ConcentrationsSection(
        title = "Concentrations",
        description = "Live contents of the node. Enter sets a value, × removes a molecule, the last row adds one.",
        nodeIds = listOf(inspector.nodeId),
        concentrations = inspector.concentrations,
        actions = actions,
    )
    InspectorSection(
        title = "Metadata",
        description = "Simulator-provided details exposed by the current adapter.",
        fields = inspector.metadata.ifEmpty {
            listOf(Labeled("Unavailable", "No extra metadata available"))
        },
    )
    InspectorSection(
        title = "Position",
        description = "Coordinates in simulator space.",
        fields = inspector.position,
    )
}

@Composable
private fun GroupNodeInspector(inspector: NodeGroup, actions: InspectorActions) {
    InspectorSection(
        title = "Selected Nodes",
        description = "IDs of the selected nodes.",
        fields = listOf(Labeled("IDs", inspector.nodeIds.formatIdRanges())),
    )
    ConcentrationsSection(
        title = "Concentrations",
        description = "Values shared by the selected nodes; Mixed means they differ or are missing. " +
            "Edits apply to every selected node; ▸ shows and edits the value in each node.",
        nodeIds = inspector.nodeIds,
        concentrations = inspector.concentrations,
        actions = actions,
        valuesByNode = inspector.valuesByNode,
    )
    InspectorSection(
        title = "Position",
        description = "Group bounds in simulator space.",
        fields = inspector.position,
    )
}

private val InspectorState.title: String
    get() =
        when (this) {
            is NodeGroup -> title
            is SingleNode -> title
        }

private val InspectorState.subtitle: String
    get() =
        when (this) {
            is NodeGroup -> subtitle
            is SingleNode -> subtitle
        }

private fun Typography.scaled(factor: Float): Typography = copy(
    h6 = h6.scaled(factor),
    subtitle1 = subtitle1.scaled(factor),
    body2 = body2.scaled(factor),
    caption = caption.scaled(factor),
    button = button.scaled(factor),
)

private fun TextStyle.scaled(factor: Float): TextStyle =
    if (fontSize.isSpecified) copy(fontSize = fontSize * factor) else this
