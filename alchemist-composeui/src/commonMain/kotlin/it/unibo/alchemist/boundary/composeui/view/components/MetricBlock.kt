/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent

@Composable
internal fun MetricBlock(label: String, value: String) {
    ComponentSurface {
        Column(
            modifier = Modifier.padding(componentPadding),
            verticalArrangement = Arrangement.spacedBy(metricBlockSpacing),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.caption,
                color = SecondaryAccent,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Monospace),
            )
        }
    }
}

private val metricBlockSpacing = 4.dp
