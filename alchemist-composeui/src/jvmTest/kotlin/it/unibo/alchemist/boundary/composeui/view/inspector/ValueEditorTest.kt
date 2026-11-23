/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import it.unibo.alchemist.boundary.composeui.model.NodeValue
import it.unibo.alchemist.boundary.composeui.model.ValueEditorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalTestApi::class)
class ValueEditorTest {
    @Test
    fun `every change typed is sent at once, and opening sends nothing`() = runComposeUiTest {
        val sent = mutableListOf<String>()
        setContent {
            EditorOfX(onEdit = { text ->
                sent += text
                Result.success(Unit)
            })
        }
        waitForIdle()
        assertEquals(emptyList<String>(), sent)
        onNodeWithContentDescription("New value of x").performTextReplacement("4")
        waitForIdle()
        onNodeWithContentDescription("New value of x").performTextInput("2")
        waitForIdle()
        assertEquals(listOf("4", "42"), sent)
    }

    @Test
    fun `a refused value is explained under the box`() = runComposeUiTest {
        setContent { EditorOfX(onEdit = { Result.failure(IllegalArgumentException("not a number")) }) }
        onNodeWithContentDescription("New value of x").performTextReplacement("oops")
        waitForIdle()
        onNodeWithText("Not applied: not a number").assertExists()
    }

    @Test
    fun `close closes the editor`() = runComposeUiTest {
        var closed = false
        setContent { EditorOfX(onClose = { closed = true }) }
        onNodeWithText("Close").performClick()
        assertTrue(closed)
    }

    private companion object {
        @Composable
        fun EditorOfX(onEdit: suspend (String) -> Result<Unit> = { Result.success(Unit) }, onClose: () -> Unit = {}) {
            ValueEditor(
                editor = ValueEditorState("x", persistentListOf(NodeValue(nodeId = 1, holds = true, value = 1.5))),
                onEdit = onEdit,
                onClose = onClose,
            )
        }
    }
}
