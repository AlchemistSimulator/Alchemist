/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable

/**
 * Modal error state shown by the shared UI.
 *
 * @property title the dialog title.
 * @property message the error message.
 */
@Immutable
data class ControlDialogState(val title: String, val message: String)
