/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Application entry point, rendered consistently across supported platforms.
 */
@Composable
fun app(controller: ComposeUiController = remember { demoController() }) {
    AlchemistUiRoot(
        state = controller.store.state,
        callbacks = controller.callbacks,
    )
}
