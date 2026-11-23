/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.callbacks.NoOpUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.app
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalTestApi::class)
class ValueEditorPlacementTest {
    @Test
    fun `the value editor stays left of a widened inspector`() = runComposeUiTest {
        val node = ViewportNode(
            id = 1,
            coordinates = persistentListOf(0.0, 0.0),
            concentrations = persistentListOf(Labeled("x", "a long value")),
        )
        val state = AlchemistUiState(scene = ViewportScene(nodes = persistentListOf(node)))
            .withSelection(listOf(1))
            .withValueEditorToggled(listOf(1), "x")
        setContent { app(ComposeUiController(state) { NoOpUiCallbacks }) }
        // As wide as the inspector can get.
        onNodeWithContentDescription("Resize the inspector").performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(-WIDENING_PX / 2, 0f))
            moveBy(Offset(-WIDENING_PX / 2, 0f))
            release()
        }
        waitForIdle()
        val inspectorEdge = onNodeWithContentDescription("Resize the inspector").fetchSemanticsNode().boundsInRoot.left
        val editorEdge = onNodeWithContentDescription("New value of x").fetchSemanticsNode().boundsInRoot.right
        assertTrue(editorEdge <= inspectorEdge, "The editor ends at $editorEdge, past the inspector at $inspectorEdge")
    }

    private companion object {
        const val WIDENING_PX = 4000f
    }
}
