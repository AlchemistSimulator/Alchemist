/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl

import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * Simple implementation of a monitor that graphically represents a 2D space and simulation.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
</T> */
class FX2DDisplay<T, P : Position2D<P>> : BaseFXDisplay<T, P>()
