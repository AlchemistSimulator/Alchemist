/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("ktlint:standard:property-naming", "ktlint:standard:function-naming")

package it.unibo.alchemist.boundary.composeui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

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
