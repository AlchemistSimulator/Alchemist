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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.NodeValue
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ConcentrationsSectionTest {
    @Test
    fun `expanding a molecule shows its value in each node`() = runComposeUiTest {
        setContent { GroupConcentrations() }
        onNodeWithText("Node 20, absent").assertDoesNotExist()
        onNodeWithContentDescription("Show x in each node").performClick()
        onNodeWithText("Node 10").assertExists()
        onNodeWithText("Node 20, absent").assertExists()
        onNodeWithContentDescription("Hide x in each node").performClick()
        onNodeWithText("Node 10").assertDoesNotExist()
    }

    @Test
    fun `a value edited in one node is set in that node only`() = runComposeUiTest {
        val edits = mutableListOf<Triple<List<Int>, String, String>>()
        setContent {
            GroupConcentrations(onSet = { nodeIds, molecule, value -> edits += Triple(nodeIds, molecule, value) })
        }
        onNodeWithContentDescription("Show x in each node").performClick()
        val field = onNodeWithContentDescription("x in node 20")
        field.performTextReplacement("7")
        field.performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        assertEquals(listOf(Triple(listOf(20), "x", "7")), edits)
    }

    @Test
    fun `a molecule removed from one node is removed from that node only`() = runComposeUiTest {
        val removals = mutableListOf<Pair<List<Int>, String>>()
        setContent { GroupConcentrations(onRemove = { nodeIds, molecule -> removals += nodeIds to molecule }) }
        onNodeWithContentDescription("Show x in each node").performClick()
        onNodeWithContentDescription("Remove x from node 10").performClick()
        onNodeWithContentDescription("Remove x from node 20").assertDoesNotExist()
        assertEquals(listOf(listOf(10) to "x"), removals)
    }

    @Test
    fun `the open buttons open the whole group or a single node in the large editor`() = runComposeUiTest {
        val opened = mutableListOf<Pair<List<Int>, String>>()
        setContent { GroupConcentrations(onOpen = { nodeIds, molecule -> opened += nodeIds to molecule }) }
        onNodeWithContentDescription("Open x in the large editor").performClick()
        onNodeWithContentDescription("Show x in each node").performClick()
        onNodeWithContentDescription("Open x of node 20 in the large editor").performClick()
        assertEquals(listOf(listOf(10, 20) to "x", listOf(20) to "x"), opened)
    }

    private companion object {
        /** Nodes 10 and 20: only node 10 holds x. Enter gives the focus back to a stand-in viewport. */
        @Composable
        fun GroupConcentrations(
            onSet: (List<Int>, String, String) -> Unit = { _, _, _ -> },
            onRemove: (List<Int>, String) -> Unit = { _, _ -> },
            onOpen: (List<Int>, String) -> Unit = { _, _ -> },
        ) {
            val viewportFocus = remember { FocusRequester() }
            CompositionLocalProvider(LocalViewportFocus provides viewportFocus) {
                Column {
                    Box(modifier = Modifier.focusRequester(viewportFocus).focusable())
                    ConcentrationsSection(
                        title = "Concentrations",
                        description = "",
                        nodeIds = listOf(10, 20),
                        concentrations = listOf(Labeled("x", "Mixed")),
                        actions = InspectorActions(
                            onDismiss = {},
                            onConcentrationSet = onSet,
                            onMoleculeRemoved = onRemove,
                            onValueEditorToggled = onOpen,
                        ),
                        valuesByNode = mapOf("x" to listOf(NodeValue(10, true, 1.0), NodeValue(20, false))),
                    )
                }
            }
        }
    }
}
