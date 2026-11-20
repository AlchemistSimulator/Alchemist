/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.resources.Res
import it.unibo.alchemist.boundary.composeui.resources.alchemist_logo
import org.jetbrains.compose.resources.painterResource

/** The logo and title, the scene message and the summary bar, over the top-left of the viewport. */
@Composable
internal fun ViewportChrome(scene: ViewportScene, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.alchemist_logo),
                contentDescription = "Alchemist logo",
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "Alchemist Simulator",
                style = MaterialTheme.typography.h4,
            )
        }
        Text(
            text = scene.message,
            style = MaterialTheme.typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        SummaryRail(
            summary = scene.summary,
            linkRenderNotice = scene.linkRenderNotice.takeIf { scene.showLinks },
        )
    }
}
