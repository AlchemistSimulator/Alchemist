/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects

import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Position2D
import java.awt.Graphics2D
import java.util.function.Function
import java.util.stream.Stream

/**
 * Defines an object capable of mapping a Layer<T, P> to a Function<* in P, * out Number>.
 */
interface LayerToFunctionMapper {
    /**
     * Prepare the mapping (if necessary).
     */
    fun <T, P : Position2D<P>> prepare(
        effect: DrawLayersValues,
        toDraw: Collection<Layer<T, P>>,
        env: Environment<T, P>,
        g: Graphics2D,
        wormhole: Wormhole2D<P>
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
