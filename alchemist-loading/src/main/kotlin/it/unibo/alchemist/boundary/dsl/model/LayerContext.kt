/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position

/**
 * Context interface for configuring spatial layers in a simulation.
 *
 * Layers define overlays of data that can be sensed everywhere in the environment.
 * They can be used to model physical properties such as pollution, light, temperature, etc.
 *
 * ## Usage Example
 *
 * ```kotlin
*     layer {
*         molecule = "A"
*         layer = StepLayer(2.0, 2.0, 100.0, 0.0)
*     }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [SimulationContext.layer] for adding layers to a simulation
 * @see [Layer] for the layer interface
 */
@AlchemistDsl
interface LayerContext<T, P : Position<P>> {
    /**
     * The molecule name associated with this layer.
     *
     */
    var molecule: String?

    /**
     * The layer instance that provides spatial data.
     *
     * @see [Layer]
     */
    var layer: Layer<T, P>?
}
