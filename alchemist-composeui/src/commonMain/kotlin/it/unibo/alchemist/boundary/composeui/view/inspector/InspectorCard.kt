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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.components.HelpTooltip
import it.unibo.alchemist.boundary.composeui.view.theme.Surface
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary

private const val CARD_ALPHA = 0.72f

/** A card of the inspector, with a [title], a [description] in a tooltip next to it, and [content] below them. */
@Composable
internal fun InspectorCard(title: String, description: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Surface.copy(alpha = CARD_ALPHA),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle1)
                if (description.isNotBlank()) {
                    HelpTooltip(description)
                }
            }
            content()
        }
    }
}
