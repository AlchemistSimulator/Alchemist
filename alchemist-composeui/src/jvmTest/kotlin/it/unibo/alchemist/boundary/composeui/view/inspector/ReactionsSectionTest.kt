/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ReactionsSectionTest {
    @Test
    fun `a single node shows its reactions right away`() = runComposeUiTest {
        setContent { Reactions(mapOf(1 to listOf("Event@1.0"))) }
        onNodeWithText("Event@1.0").assertExists()
    }

    @Test
    fun `in a group, each node expands to show its reactions`() = runComposeUiTest {
        setContent { Reactions(mapOf(1 to listOf("Event@1.0"), 2 to null)) }
        onNodeWithText("Node 1, 1 reactions").assertExists()
        onNodeWithText("Event@1.0").assertDoesNotExist()
        onNodeWithContentDescription("Show the reactions of node 1").performClick()
        onNodeWithText("Event@1.0").assertExists()
        onNodeWithContentDescription("Show the reactions of node 2").performClick()
        onNodeWithText("Loading…").assertExists()
    }

    private companion object {
        @Composable
        fun Reactions(reactionsByNode: Map<Int, List<String>?>) {
            val viewportFocus = remember { FocusRequester() }
            CompositionLocalProvider(LocalViewportFocus provides viewportFocus) {
                Column {
                    Box(modifier = Modifier.focusRequester(viewportFocus).focusable())
                    ReactionsSection(title = "Reactions", description = "", reactionsByNode = reactionsByNode)
                }
            }
        }
    }
}
