/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

@Composable
internal fun SummaryRail(
    summary: List<InfoField>,
    showLinks: Boolean,
    onToggleLinks: () -> Unit,
    linkRenderNotice: String? = null,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        summary.forEach { item ->
            SummaryChip(label = item.label.uppercase(), value = item.value)
        }
        linkRenderNotice?.let { notice ->
            SummaryChip(
                label = "ADAPTIVE",
                value = notice,
                background = SecondaryAccent.copy(alpha = 0.12f),
                labelColor = SecondaryAccent,
            )
        }
        SummaryChip(
            label = "LINKS",
            value = if (showLinks) "ON" else "OFF",
            background = if (showLinks) SecondaryAccent.copy(alpha = 0.2f) else SurfaceStrong.copy(alpha = 0.82f),
            labelColor = if (showLinks) SecondaryAccent else TextSecondary,
            modifier = Modifier
                .toggleable(
                    value = showLinks,
                    role = Role.Switch,
                    onValueChange = { onToggleLinks() },
                )
                .semantics {
                    stateDescription = if (showLinks) "Links visible" else "Links hidden"
                },
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    background: Color = SurfaceStrong.copy(alpha = 0.82f),
    labelColor: Color = TextSecondary,
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = RoundedCornerShape(999.dp),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.caption,
                color = labelColor,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}
