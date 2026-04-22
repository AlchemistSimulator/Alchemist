/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
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

/**
 * Main shared screen for the simulator UI.
 */
@Composable
fun AlchemistUiRoot(state: AlchemistUiState, callbacks: AlchemistUiCallbacks) {
    val coroutineScope = rememberCoroutineScope()
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = PrimaryAccent,
            primaryVariant = SecondaryAccent,
            secondary = SecondaryAccent,
            background = Background,
            surface = Surface,
            onPrimary = Background,
            onSecondary = Background,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        ),
        typography = MaterialTheme.typography.copy(
            h4 = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            h6 = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            ),
            body2 = MaterialTheme.typography.body2.copy(
                color = TextSecondary,
            ),
            caption = MaterialTheme.typography.caption.copy(
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
            ),
            button = MaterialTheme.typography.button.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        ),
    ) {
        state.controls.dialog?.let { dialog ->
            androidx.compose.material.AlertDialog(
                onDismissRequest = { coroutineScope.launch { callbacks.onDialogDismiss() } },
                title = { Text(dialog.title) },
                text = { Text(dialog.message) },
                confirmButton = {
                    androidx.compose.material.TextButton(
                        onClick = { coroutineScope.launch { callbacks.onDialogDismiss() } },
                    ) {
                        Text("OK")
                    }
                },
                backgroundColor = SurfaceStrong,
                contentColor = TextPrimary,
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Background, BackgroundVariant, BackgroundGradientEnd),
                    ),
                ),
        ) {
            val compactLayout = maxWidth < 980.dp
            val inspectorVisible = state.inspector != null
            val inspectorWidth = 324.dp
            val bottomBarHeight = 152.dp
            val layoutSpacing = 20.dp
            val inspectorTransition = updateTransition(targetState = inspectorVisible, label = "inspector")
            val animatedInspectorWidth by inspectorTransition.animateDp(
                transitionSpec = { tween(durationMillis = 320) },
                label = "inspector-width",
            ) { visible ->
                if (visible) {
                    inspectorWidth
                } else {
                    0.dp
                }
            }
            var displayedInspector by remember { mutableStateOf(state.inspector) }
            if (state.inspector != null) {
                displayedInspector = state.inspector
            }
            if (!inspectorTransition.currentState && !inspectorTransition.targetState && displayedInspector != null) {
                displayedInspector = null
            }
            if (compactLayout) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(layoutSpacing),
                ) {
                    SimulationPrimaryPane(
                        scene = state.scene,
                        controls = state.controls,
                        selectedNodeId = state.selectedNodeId,
                        callbacks = callbacks,
                        dockWidthFraction = 1f,
                        spacing = layoutSpacing,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (inspectorVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x66050A11))
                                .clickable(onClick = { coroutineScope.launch { callbacks.onInspectorDismiss() } }),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = bottomBarHeight + 12.dp)
                                .fillMaxWidth(),
                        ) {
                            NodeInspector(
                                inspector = requireNotNull(state.inspector),
                                onDismiss = { coroutineScope.launch { callbacks.onInspectorDismiss() } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(layoutSpacing),
                    horizontalArrangement = Arrangement.spacedBy(layoutSpacing),
                ) {
                    SimulationPrimaryPane(
                        scene = state.scene,
                        controls = state.controls,
                        selectedNodeId = state.selectedNodeId,
                        callbacks = callbacks,
                        dockWidthFraction = 0.84f,
                        spacing = layoutSpacing,
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(animationSpec = tween(durationMillis = 320))
                            .fillMaxHeight(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(animatedInspectorWidth),
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = inspectorVisible,
                            enter = slideInHorizontally(
                                animationSpec = tween(durationMillis = 320),
                                initialOffsetX = { it / 3 },
                            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
                            exit = slideOutHorizontally(
                                animationSpec = tween(durationMillis = 320),
                                targetOffsetX = { it / 3 },
                            ) + fadeOut(animationSpec = tween(durationMillis = 180)),
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            displayedInspector?.let {
                                NodeInspector(
                                    inspector = it,
                                    onDismiss = { coroutineScope.launch { callbacks.onInspectorDismiss() } },
                                    modifier = Modifier.fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
