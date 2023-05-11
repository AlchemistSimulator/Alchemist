/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.state

import com.soywiz.korim.bitmap.Bitmap
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.boundary.webui.common.renderer.BitmapRenderer
import it.unibo.alchemist.boundary.webui.common.renderer.Renderer

/**
 * The common state of the client and the server.
 * This class includes common components that both systems need to have.
 * @param renderer the [Renderer] that renders an
 * [it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate].
 */
open class CommonState(
    val renderer: Renderer<Any, PositionSurrogate, Bitmap> = BitmapRenderer(),
)
