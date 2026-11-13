/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor

private const val DOCK_SECTION_SURFACE_ALPHA = 0.78f
private const val DOCK_SECTION_BORDER_ALPHA = 0.7f
private val dockSectionCornerRadius = 12.dp
private val dockSectionBorderWidth = 1.dp
private val dockSectionHorizontalPadding = 14.dp
private val dockSectionVerticalPadding = 12.dp
private val dockSectionContentSpacing = 10.dp

/** Space between the items in a dock section. */
internal val sectionItemSpacing = 12.dp

@Composable
internal fun DockSection(title: String, content: @Composable () -> Unit) {
    Surface(
        color = SurfaceColor.copy(alpha = DOCK_SECTION_SURFACE_ALPHA),
        shape = RoundedCornerShape(dockSectionCornerRadius),
        border = BorderStroke(dockSectionBorderWidth, Outline.copy(alpha = DOCK_SECTION_BORDER_ALPHA)),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dockSectionHorizontalPadding,
                vertical = dockSectionVerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(dockSectionContentSpacing),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.caption,
                color = SecondaryAccent,
                fontWeight = FontWeight.SemiBold,
            )
            Box(contentAlignment = Alignment.CenterStart) {
                content()
            }
        }
    }
}
