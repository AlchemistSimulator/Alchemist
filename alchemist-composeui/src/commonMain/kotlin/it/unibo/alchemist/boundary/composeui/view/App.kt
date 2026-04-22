/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import it.unibo.alchemist.boundary.composeui.ComposeUiController
import it.unibo.alchemist.boundary.composeui.demoController
import it.unibo.alchemist.boundary.composeui.view.root.AlchemistUiRoot

/**
 * Application entry point, rendered consistently across supported platforms.
 */
@Composable
fun app(controller: ComposeUiController = remember { demoController() }) {
    val state by controller.store.stateFlow.collectAsState()
    AlchemistUiRoot(
        state = state,
        callbacks = controller.callbacks,
    )
}
