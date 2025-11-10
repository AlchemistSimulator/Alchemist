/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position

/**
 * Context for configuring layers in a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */
class LayerContextImpl<T, P : Position<P>> : LayerContext<T, P> {
    override var molecule: String? = null

    override var layer: Layer<T, P>? = null
}
