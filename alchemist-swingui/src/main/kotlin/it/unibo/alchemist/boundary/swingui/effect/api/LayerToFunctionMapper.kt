/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.api

import it.unibo.alchemist.boundary.swingui.effect.impl.DrawLayersValues
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import java.awt.Graphics2D
import java.io.Serializable
import java.util.function.Function
import java.util.stream.Stream

/**
 * Defines an object capable of mapping a Layer<T, P> to a Function<* in P, * out Number>.
 */
interface LayerToFunctionMapper : Serializable {
    /**
     * Prepare the mapping (if necessary).
     */
    fun <T, P : Position2D<P>> prepare(
        effect: DrawLayersValues,
        toDraw: Collection<Layer<T, P>>,
        environment: Environment<T, P>,
        g: Graphics2D,
        wormhole: Wormhole2D<P>,
    ) = Unit // defaults to nothing

    /**
     * Effectively map the given layers, layers may be filtered too if the mapper is only able
     * to map certain types of layers.
     */
    fun <T, P : Position2D<P>> map(layers: Collection<Layer<T, P>>): Collection<Function<in P, out Number>>

    /**
     * see [LayerToFunctionMapper.map].
     */
    fun <T, P : Position2D<P>> map(layers: Stream<Layer<T, P>>): Stream<Function<in P, out Number>>
}
