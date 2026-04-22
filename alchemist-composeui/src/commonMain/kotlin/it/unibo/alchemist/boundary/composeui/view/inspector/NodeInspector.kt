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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.GroupInspectorState
import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import it.unibo.alchemist.boundary.composeui.model.NodeInspectorState
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

@Composable
internal fun NodeInspector(inspector: InspectorState, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SurfaceStrong,
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
                title = when (inspector) {
                    is GroupInspectorState -> inspector.title
                    is NodeInspectorState -> inspector.title
                },
                subtitle = when (inspector) {
                    is GroupInspectorState -> inspector.subtitle
                    is NodeInspectorState -> inspector.subtitle
                },
                onDismiss = onDismiss,
            )
            when (inspector) {
                is NodeInspectorState -> SingleNodeInspector(inspector)
                is GroupInspectorState -> GroupNodeInspector(inspector)
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
private fun SingleNodeInspector(inspector: NodeInspectorState) {
    InspectorSection(
        title = "Position",
        description = "Coordinates in simulator space.",
        fields = inspector.position,
    )
    InspectorSection(
        title = "Concentrations",
        description = "Live contents currently stored in the selected node.",
        fields = inspector.concentrations.ifEmpty {
            listOf(InfoField("No molecules", "This node exposes no concentrations"))
        },
    )
    InspectorSection(
        title = "Metadata",
        description = "Simulator-provided details exposed by the current adapter.",
        fields = inspector.metadata.ifEmpty {
            listOf(InfoField("Unavailable", "No extra metadata available"))
        },
    )
}

@Composable
private fun GroupNodeInspector(inspector: GroupInspectorState) {
    InspectorSection(
        title = "Position",
        description = "Group bounds in simulator space.",
        fields = inspector.position,
    )
    InspectorSection(
        title = "Selected Nodes",
        description = "IDs currently captured by the selection box.",
        fields = listOf(InfoField("IDs", inspector.nodeIds.joinToString(", "))),
    )
    InspectorSection(
        title = "Concentrations",
        description = "Shared molecule values across the selected nodes. Mixed means values differ or are missing.",
        fields = inspector.concentrations.ifEmpty {
            listOf(InfoField("No molecules", "The selected nodes expose no concentrations"))
        },
    )
}
