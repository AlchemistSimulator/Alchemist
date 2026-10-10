/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position

/**
 * A [Layer] whose value is [level] at every position and never changes.
 *
 * @param T concentration type
 * @param P position type
 * @param level the value of the layer
 */
class ConstantLayer<T, P : Position<out P>>(private val level: T) : TimeInvariantLayer<T, P>() {
    override fun getValue(position: P): T = level
}
