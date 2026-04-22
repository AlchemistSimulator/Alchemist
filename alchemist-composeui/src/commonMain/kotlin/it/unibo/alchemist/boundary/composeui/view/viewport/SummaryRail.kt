/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

@Composable
internal fun SummaryRail(summary: List<InfoField>, showLinks: Boolean, onToggleLinks: () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        summary.forEach { item ->
            Surface(
                color = SurfaceStrong.copy(alpha = 0.82f),
                shape = RoundedCornerShape(999.dp),
                elevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.caption,
                        color = TextSecondary,
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.clickable(onClick = onToggleLinks),
            color = if (showLinks) SecondaryAccent.copy(alpha = 0.2f) else SurfaceStrong.copy(alpha = 0.82f),
            shape = RoundedCornerShape(999.dp),
            elevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LINKS",
                    style = MaterialTheme.typography.caption,
                    color = if (showLinks) SecondaryAccent else TextSecondary,
                )
                Text(
                    text = if (showLinks) "ON" else "OFF",
                    style = MaterialTheme.typography.subtitle1,
                )
            }
        }
    }
}
