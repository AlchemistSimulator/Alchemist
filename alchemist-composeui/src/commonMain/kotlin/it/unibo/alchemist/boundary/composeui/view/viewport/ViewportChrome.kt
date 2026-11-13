/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ViewportScene

/** The title, the scene message and the summary bar, over the top-left of the viewport. */
@Composable
internal fun ViewportChrome(scene: ViewportScene, onToggleLinks: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Alchemist Simulator",
            style = MaterialTheme.typography.h4,
        )
        Text(
            text = scene.message,
            style = MaterialTheme.typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        SummaryRail(
            summary = scene.summary,
            showLinks = scene.showLinks,
            onToggleLinks = onToggleLinks,
            linkRenderNotice = scene.linkRenderNotice.takeIf { scene.showLinks },
        )
    }
}
