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

/** One neutral color for every node, until effects choose colors. */
internal val NodeColor = Color(0xFF495057)
internal val InspectorScrim = Color(0x66050A11)
internal const val ZOOM_STEP = 1.12f
internal const val NODE_HIT_RADIUS = 22f
internal const val SELECTED_NODE_RADIUS = 18f
internal const val SELECTED_NODE_INNER_RADIUS = 12f
internal const val NODE_RADIUS = 7f
internal const val LINK_STROKE_WIDTH = 1.5f
internal val StatusPillWidth = 132.dp
internal const val GRID_VERTICAL_DIVISIONS = 8
internal const val GRID_HORIZONTAL_DIVISIONS = 6
