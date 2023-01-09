/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.renderer

import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate

/**
 * Renderer of an [EnvironmentSurrogate].
 * @param <TS> the type of the concentration surrogate.
 * @param <PS> the type of the position surrogate.
 * @param <R> the type of the result, presumably a graphic representation, like a raster image or an SVG string.
 */
interface Renderer<in TS : Any, in PS : PositionSurrogate, out R> {

    /**
     * Renders the [EnvironmentSurrogate].
     * @param environmentSurrogate the [EnvironmentSurrogate] to render.
     * @return the result of the rendering.
     */
    fun render(environmentSurrogate: EnvironmentSurrogate<TS, PS>): R
}
