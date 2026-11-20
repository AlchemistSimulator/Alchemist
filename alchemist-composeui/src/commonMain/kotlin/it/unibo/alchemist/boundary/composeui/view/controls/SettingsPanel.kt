/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus
import kotlin.math.roundToInt

private const val FPS_CAP = "FPS cap"

private val fpsFieldWidth = 56.dp
private val fpsSliderWidth = 160.dp
private val panelPadding = 12.dp
private val panelCornerRadius = 12.dp

/**
 * The display settings: the UI frame rate cap, applied as it changes, and whether links are drawn.
 * It is part of the overlay, not a popup, so the viewport behind it keeps receiving the pointer.
 * Its controls give the focus back to the viewport once used, so the keyboard shortcuts keep working.
 */
@Composable
internal fun SettingsPanel(
    controls: SimulationControlsState,
    showLinks: Boolean,
    onFpsInputChanged: (String) -> Unit,
    onFpsSubmit: () -> Unit,
    onToggleLinks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewportFocus = LocalViewportFocus.current
    Surface(
        modifier = modifier,
        color = FloatingPanel,
        shape = RoundedCornerShape(panelCornerRadius),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(panelPadding),
            verticalArrangement = Arrangement.spacedBy(stripItemSpacing),
        ) {
            Text(text = FPS_CAP, style = MaterialTheme.typography.caption, color = SecondaryAccent)
            Row(
                horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = controls.uiFps.toFloat(),
                    onValueChange = { onFpsInputChanged(it.roundToInt().toString()) },
                    onValueChangeFinished = { viewportFocus.requestFocus() },
                    modifier = Modifier
                        .width(fpsSliderWidth)
                        .semantics { contentDescription = FPS_CAP },
                    valueRange = MIN_UI_FPS.toFloat()..controls.maxUiFps.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryAccent,
                        activeTrackColor = PrimaryAccent,
                        inactiveTrackColor = Outline,
                    ),
                )
                DockTextField(
                    label = null,
                    value = controls.fpsInput,
                    error = DockValidationMessages.of(controls).fps,
                    onValueChange = onFpsInputChanged,
                    onSubmit = onFpsSubmit,
                    width = fpsFieldWidth,
                )
            }
            Row(
                modifier = Modifier.toggleable(
                    value = showLinks,
                    role = Role.Switch,
                    onValueChange = {
                        onToggleLinks()
                        viewportFocus.requestFocus()
                    },
                ),
                horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "links", style = MaterialTheme.typography.caption, color = SecondaryAccent)
                Switch(
                    checked = showLinks,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAccent),
                )
            }
        }
    }
}
