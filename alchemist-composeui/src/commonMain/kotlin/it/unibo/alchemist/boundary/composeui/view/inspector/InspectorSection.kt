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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.toDisplayString
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

/** How visible the lines between rows are. */
internal const val DIVIDER_ALPHA = 0.42f

/** An inspector card listing [fields], one per row. */
@Composable
internal fun InspectorSection(title: String, description: String, fields: List<Labeled<*>>) {
    InspectorCard(title, description) {
        fields.forEachIndexed { index, field ->
            if (index > 0) {
                Divider(color = Outline.copy(alpha = DIVIDER_ALPHA))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.body2,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = field.value.toDisplayString(),
                    style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}
