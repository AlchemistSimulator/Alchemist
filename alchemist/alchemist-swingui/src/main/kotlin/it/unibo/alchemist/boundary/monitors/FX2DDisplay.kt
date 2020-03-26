/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * Simple implementation of a monitor that graphically represents a 2D space and simulation.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
</T> */
class FX2DDisplay<T, P : Position2D<P>>
@JvmOverloads constructor(step: Int = AbstractFXDisplay.DEFAULT_NUMBER_OF_STEPS) : AbstractFXDisplay<T, P>(step)
