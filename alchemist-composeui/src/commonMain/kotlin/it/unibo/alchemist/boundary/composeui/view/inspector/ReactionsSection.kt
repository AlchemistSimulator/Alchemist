/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.inspector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

private val entrySpacing = 4.dp
private val nodeRowIndent = 16.dp

/**
 * An inspector card listing, read-only, the reactions of each node in [reactionsByNode].
 * A single node shows them right away; in a group, each node expands to show its own.
 * Reactions still `null` are on their way from the simulation.
 */
@Composable
internal fun ReactionsSection(title: String, description: String, reactionsByNode: Map<Int, List<String>?>) {
    InspectorCard(title, description) {
        Column(verticalArrangement = Arrangement.spacedBy(entrySpacing)) {
            when (reactionsByNode.size) {
                1 -> ReactionList(reactionsByNode.values.single())
                else -> reactionsByNode.forEach { (nodeId, reactions) ->
                    key(nodeId) {
                        NodeReactions(nodeId, reactions)
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeReactions(nodeId: Int, reactions: List<String>?) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(entrySpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphButton(
            glyph = if (expanded) "▾" else "▸",
            description = if (expanded) "Hide the reactions of node $nodeId" else "Show the reactions of node $nodeId",
            onClick = { expanded = !expanded },
        )
        Text(
            text = "Node $nodeId" + reactions?.let { ", ${it.size} reactions" }.orEmpty(),
            style = MaterialTheme.typography.body2,
            color = TextSecondary,
        )
    }
    if (expanded) {
        Column(
            modifier = Modifier.padding(start = nodeRowIndent),
            verticalArrangement = Arrangement.spacedBy(entrySpacing),
        ) {
            ReactionList(reactions)
        }
    }
}

@Composable
private fun ReactionList(reactions: List<String>?) {
    when {
        reactions == null -> Placeholder("Loading…")
        reactions.isEmpty() -> Placeholder("No reactions")
        // Selectable, so that long reactions can be copied.
        else -> SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(entrySpacing)) {
                reactions.forEachIndexed { index, reaction ->
                    if (index > 0) {
                        Divider(color = Outline.copy(alpha = DIVIDER_ALPHA))
                    }
                    Text(text = reaction, style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Text(text = text, style = MaterialTheme.typography.body2, color = TextSecondary)
}
