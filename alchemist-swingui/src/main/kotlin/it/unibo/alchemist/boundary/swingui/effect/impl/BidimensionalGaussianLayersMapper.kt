/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("DEPRECATION")

package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.swingui.effect.api.LayerToFunctionMapper
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.layers.BidimensionalGaussianLayer
import java.awt.Graphics2D
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Maps [BidimensionalGaussianLayer]s, it ignores any other layer.
 *
 * This class also manages to infer optimal min and max layer values automatically
 * so the user does not have to set them by hand.
 */
@Deprecated("The entire Swing UI is deprecated and is set to be replaced with a modern UI")
class BidimensionalGaussianLayersMapper : LayerToFunctionMapper {
    private var minAndMaxToBeSet = true

    override fun <T, P : Position2D<P>> prepare(
        effect: AbstractDrawLayersValues,
        toDraw: Collection<Layer<T, P>>,
        environment: Environment<T, P>,
        g: Graphics2D,
        wormhole: Wormhole2D<P>,
    ) {
        if (minAndMaxToBeSet) {
            val maxLayerValue =
                toDraw
                    .stream()
                    .filter { l -> l is BidimensionalGaussianLayer }
                    .map { l -> l as BidimensionalGaussianLayer }
                    .map { l -> l.getValue(environment.makePosition(l.centerX, l.centerY)) }
                    .max { d1, d2 -> java.lang.Double.compare(d1, d2) }
                    .orElse(MINIMUM_LAYER_VALUE)
            effect.minLayerValue = MINIMUM_LAYER_VALUE.toString()
            effect.maxLayerValue = maxLayerValue.toString()
            minAndMaxToBeSet = false
        }
    }

    override fun <T, P : Position2D<P>> map(layers: Collection<Layer<T, P>>): Collection<Function<in P, out Number>> =
        map(layers.stream()).collect(Collectors.toList())

    override fun <T, P : Position2D<P>> map(layers: Stream<Layer<T, P>>): Stream<Function<in P, out Number>> =
        layers
            .filter { l -> l is BidimensionalGaussianLayer }
            .map { l -> l as BidimensionalGaussianLayer }
            .map { l -> Function { p: P -> l.getValue(p) } }

    /**
     * Utilities for [BidimensionalGaussianLayersMapper]s.
     */
    companion object {
        /**
         * The minumum value of any layer.
         */
        const val MINIMUM_LAYER_VALUE = 0.1
    }
}
