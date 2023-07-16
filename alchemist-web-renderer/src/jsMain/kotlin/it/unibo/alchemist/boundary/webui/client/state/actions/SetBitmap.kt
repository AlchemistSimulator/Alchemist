/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state.actions

import com.soywiz.korim.bitmap.Bitmap

/**
 * Redux action to set the bitmap to display.
 * @param bitmap the new bitmap to set.
 */
data class SetBitmap(val bitmap: Bitmap?)
