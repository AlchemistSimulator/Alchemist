/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state.reducers

import com.soywiz.korim.bitmap.Bitmap
import it.unibo.alchemist.boundary.webui.client.state.actions.SetBitmap

/**
 * Reducer for the bitmap.
 * @param state the current bitmap state.
 * @param action the action to perform.
 * @return the new bitmap.
 */
fun bitmapReducer(state: Bitmap?, action: Any): Bitmap? = when (action) {
    is SetBitmap -> action.bitmap
    else -> state
}
