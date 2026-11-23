/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ValueEditorState
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

private const val REFUSAL_LINES = 4
private const val TYPING_HINT = "Changes apply as you type."

private val panelPadding = 22.dp
private val boxPadding = 10.dp
private val editorMinHeight = 120.dp
private val boxShape = RoundedCornerShape(6.dp)
private val monospace = FontFamily.Monospace

/**
 * A large panel over the viewport to read and edit one molecule of [editor], for long values.
 * It shows the live value and its type; every change typed is sent at once through [onEdit],
 * and what the simulation refuses is explained under the box. Escape or Close call [onClose].
 */
@Composable
internal fun ValueEditor(
    editor: ValueEditorState,
    onEdit: suspend (String) -> Result<Unit>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val target = editor.molecule to editor.nodeIds
    var draft by remember(target) { mutableStateOf(editor.sharedText) }
    var refusal by remember(target) { mutableStateOf<String?>(null) }
    val currentOnEdit by rememberUpdatedState(onEdit)
    LaunchedEffect(target) {
        // The first text is the current value: only what the user types is sent, the latest first.
        snapshotFlow { draft }.drop(1).collectLatest { text ->
            refusal = currentOnEdit(text).exceptionOrNull()?.let { it.message ?: it.toString() }
        }
    }
    Surface(
        modifier = modifier.onPreviewKeyEvent { event ->
            val isEscape = event.key == Key.Escape
            if (isEscape && event.type == KeyEventType.KeyUp) {
                onClose()
            }
            isEscape
        },
        color = FloatingPanel,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(panelPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = editor.molecule, style = MaterialTheme.typography.h6)
                    Text(text = editor.subtitle, style = MaterialTheme.typography.body2)
                }
                TransportButton(label = "Close", enabled = true, accent = Outline, onClick = onClose)
            }
            EditorField(title = "New value") {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = editorMinHeight)
                        .semantics { contentDescription = "New value of ${editor.molecule}" }
                        .border(width = 1.dp, color = if (refusal == null) Outline else Danger, shape = boxShape)
                        .padding(boxPadding),
                    textStyle = MaterialTheme.typography.body2.copy(color = TextPrimary, fontFamily = monospace),
                    cursorBrush = SolidColor(PrimaryAccent),
                )
                Text(
                    text = refusal?.let { "Not applied: $it" } ?: TYPING_HINT,
                    style = MaterialTheme.typography.caption,
                    color = if (refusal == null) TextSecondary else Danger,
                    maxLines = REFUSAL_LINES,
                )
            }
            EditorField(title = "Type") { Text(text = editor.typeText, style = MaterialTheme.typography.caption) }
            EditorField(title = "Current value") {
                SelectionContainer {
                    Text(text = editor.valueText, style = MaterialTheme.typography.body2.copy(fontFamily = monospace))
                }
            }
        }
    }
}

@Composable
private fun EditorField(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        content()
    }
}
