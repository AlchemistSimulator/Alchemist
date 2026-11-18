/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

private val DockTextFieldWidth = 132.dp

/**
 * A dock text box that submits on Enter.
 * With a [default], the box shows it until focused, then starts editing from it, and Enter leaves the box.
 * The [error], if any, replaces the [hint] while editing.
 */
@Composable
internal fun DockTextField(
    label: String,
    value: String,
    hint: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    default: String? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val following = default != null && !focused
    val isError = error != null && !following
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedTextField(
            value = default?.takeIf { following } ?: value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(DockTextFieldWidth)
                .onFocusChanged { focus ->
                    if (focus.isFocused && !focused && default != null) {
                        onValueChange(default)
                    }
                    focused = focus.isFocused
                }
                .onPreviewKeyEvent {
                    val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
                    if (it.type == KeyEventType.KeyUp && isEnter) {
                        onSubmit()
                        default?.let { focusManager.clearFocus() }
                        true
                    } else {
                        false
                    }
                },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = TextPrimary,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = Outline,
                errorBorderColor = Danger,
                errorLabelColor = Danger,
                errorCursorColor = Danger,
                focusedLabelColor = PrimaryAccent,
                unfocusedLabelColor = TextSecondary,
                cursorColor = PrimaryAccent,
            ),
        )
        Text(
            text = error?.takeIf { isError } ?: hint,
            style = MaterialTheme.typography.caption,
            color = if (isError) Danger else TextSecondary,
        )
    }
}
