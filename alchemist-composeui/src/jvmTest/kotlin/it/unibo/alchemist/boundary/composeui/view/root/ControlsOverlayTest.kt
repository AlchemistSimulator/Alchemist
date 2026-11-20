/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.callbacks.NoOpUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ControlsOverlayTest {
    @Test
    fun `the settings button shows and hides the fps cap and the links switch`() = runComposeUiTest {
        setContent { OverlayOverViewport() }
        onNodeWithContentDescription("Display settings").performClick()
        onNodeWithText("FPS cap").assertExists()
        onNodeWithText("links").assertExists()
        onNodeWithContentDescription("Display settings").performClick()
        waitForIdle()
        onNodeWithText("FPS cap").assertDoesNotExist()
    }

    @Test
    fun `the viewport still gets the mouse wheel while the settings are open`() = runComposeUiTest {
        var scrolls = 0
        setContent { OverlayOverViewport(onScroll = { scrolls++ }) }
        onNodeWithContentDescription("Display settings").performClick()
        onNodeWithText("FPS cap").assertExists()
        onRoot().performMouseInput {
            moveTo(Offset(1f, 1f))
            scroll(1f)
        }
        waitForIdle()
        assertEquals(1, scrolls)
    }

    @Test
    fun `the viewport gets the pan keys again after using the fps slider`() = runComposeUiTest {
        var keys = 0
        setContent { OverlayOverViewport(onKey = { keys++ }) }
        onNodeWithContentDescription("Display settings").performClick()
        onNodeWithContentDescription("FPS cap").performMouseInput {
            click(center)
        }
        waitForIdle()
        onRoot().performKeyInput { pressKey(Key.A) }
        waitForIdle()
        assertEquals(1, keys)
    }

    private companion object {
        /**
         * The controls overlay over a stand-in for the viewport,
         * which takes the focus and counts wheel events and pressed keys.
         */
        @Composable
        fun OverlayOverViewport(onScroll: () -> Unit = {}, onKey: () -> Unit = {}) {
            val viewportFocus = remember { FocusRequester() }
            LaunchedEffect(viewportFocus) { viewportFocus.requestFocus() }
            CompositionLocalProvider(LocalViewportFocus provides viewportFocus) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(viewportFocus)
                            .focusable()
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    onKey()
                                }
                                false
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        if (awaitPointerEvent().type == PointerEventType.Scroll) {
                                            onScroll()
                                        }
                                    }
                                }
                            },
                    )
                    ControlsOverlay(
                        controls = SimulationControlsState(),
                        nodeCount = 0,
                        showLinks = false,
                        callbacks = NoOpUiCallbacks,
                        dockVisible = true,
                        onToggleDock = {},
                        helpVisible = false,
                        onToggleHelp = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
