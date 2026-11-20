/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.components.TransportButtonSize
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus

private val fieldPadding = 8.dp

/**
 * A compact dock text box, with an optional [label] inside.
 * Enter submits it and hands the focus back to the viewport.
 * With a [default], the box shows it until focused, then starts editing from it,
 * and Enter or leaving the box clears the text, so that the box follows the default again.
 * While editing, an [error] turns the border red.
 */
@Composable
internal fun DockTextField(
    label: String?,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    width: Dp,
    default: String? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val viewportFocus = LocalViewportFocus.current
    val following = default != null && !focused
    val shownError = error?.takeUnless { following }
    val borderColor = when {
        shownError != null -> Danger
        focused -> PrimaryAccent
        else -> Outline
    }
    BasicTextField(
        value = default?.takeIf { following } ?: value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(width)
            .height(TransportButtonSize)
            .semantics { shownError?.let { error(it) } }
            .onFocusChanged { focus ->
                if (default != null && focus.isFocused != focused) {
                    onValueChange(if (focus.isFocused) default else "")
                }
                focused = focus.isFocused
            }
            .onPreviewKeyEvent {
                val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
                if (it.type == KeyEventType.KeyUp && isEnter) {
                    onSubmit()
                    viewportFocus.requestFocus()
                    true
                } else {
                    false
                }
            },
        singleLine = true,
        textStyle = MaterialTheme.typography.body2.copy(color = TextPrimary, fontFamily = FontFamily.Monospace),
        cursorBrush = SolidColor(PrimaryAccent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 1.dp, color = borderColor, shape = componentShape)
                    .padding(horizontal = fieldPadding),
                horizontalArrangement = Arrangement.spacedBy(fieldPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                label?.let { Text(text = it, style = MaterialTheme.typography.caption, color = SecondaryAccent) }
                Box(modifier = Modifier.weight(1f)) { innerTextField() }
            }
        },
    )
}
