/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalTestApi::class)
class ResizableInspectorTest {
    @Test
    fun `dragging the edge to the left widens the inspector`() = runComposeUiTest {
        var width by mutableStateOf(START_WIDTH)
        setContent { InspectorAtTheRight(width) { width = it } }
        dragHandleBy(-DRAG_PX)
        assertTrue(width > START_WIDTH, "The inspector should be wider than $START_WIDTH, but is $width")
    }

    @Test
    fun `the inspector is never narrower than 240 dp`() = runComposeUiTest {
        var width by mutableStateOf(START_WIDTH)
        setContent { InspectorAtTheRight(width) { width = it } }
        dragHandleBy(DRAG_PX)
        assertEquals(240.dp, width)
    }

    @Test
    fun `the inspector is never wider than most of the window`() = runComposeUiTest {
        var width by mutableStateOf(START_WIDTH)
        setContent { InspectorAtTheRight(width, availableWidth = 500.dp) { width = it } }
        dragHandleBy(-DRAG_PX)
        assertEquals(350.dp, width)
    }

    private companion object {
        const val DRAG_PX = 100f
        val START_WIDTH = 300.dp

        fun ComposeUiTest.dragHandleBy(pixels: Float) {
            onNodeWithContentDescription("Resize the inspector").performMouseInput {
                press()
                moveBy(Offset(pixels / 2, 0f))
                moveBy(Offset(pixels / 2, 0f))
                release()
            }
            waitForIdle()
        }

        @Composable
        fun InspectorAtTheRight(width: Dp, availableWidth: Dp = 2000.dp, onWidthChange: (Dp) -> Unit) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                ResizableInspector(
                    inspector = InspectorState.NodeGroup(
                        nodeIds = persistentListOf(1, 2),
                        position = persistentListOf(),
                        concentrations = persistentListOf(),
                    ),
                    width = width,
                    availableWidth = availableWidth,
                    onWidthChange = onWidthChange,
                    onDismiss = {},
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }
    }
}
