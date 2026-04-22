/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress(
    "MagicNumber",
    "TopLevelPropertyNaming",
    "ktlint:standard:property-naming",
    "ktlint:standard:function-naming",
)

package it.unibo.alchemist.boundary.composeui.view.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

internal val Background = Color(0xFFF8F9FA)
internal val BackgroundVariant = Color(0xFFE9ECEF)
internal val BackgroundGradientEnd = Color(0xFFDEE2E6)
internal val Surface = Color(0xFFFFFFFF)
internal val SurfaceStrong = Color(0xFFF1F3F5)
internal val Outline = Color(0xFFCED4DA)
internal val PrimaryAccent = Color(0xFF0D6EFD)
internal val SecondaryAccent = Color(0xFF0A58CA)
internal val Positive = Color(0xFF198754)
internal val TextPrimary = Color(0xFF212529)
internal val TextSecondary = Color(0xFF495057)
internal val TextMuted = Color(0xFF6C757D)
internal val Danger = Color(0xFFDC3545)
internal val InspectorScrim = Color(0x66050A11)
internal const val ZoomStep = 1.12f
internal const val NodeHitRadius = 22f
internal const val SelectedNodeRadius = 18f
internal const val SelectedNodeInnerRadius = 12f
internal const val NodeRadius = 7f
internal const val LinkStrokeWidth = 1.5f
internal val StatusPillWidth = 132.dp
internal const val GridVerticalDivisions = 8
internal const val GridHorizontalDivisions = 6
internal fun lerp(start: Color, end: Color, amount: Float): Color {
    val clamped = min(1f, max(0f, amount))
    return Color(
        red = start.red + (end.red - start.red) * clamped,
        green = start.green + (end.green - start.green) * clamped,
        blue = start.blue + (end.blue - start.blue) * clamped,
        alpha = start.alpha + (end.alpha - start.alpha) * clamped,
    )
}
