/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatiotemporal

import it.unibo.alchemist.model.geogrid.strategy.spatial.BilinearInterpolation
import it.unibo.alchemist.model.geogrid.strategy.temporal.LinearInterpolation
import it.unibo.alchemist.model.geogrid.utils.StatelessStrategy

/**
 * Trilinear interpolation of the eight nodes around the position and time, missing if any of them is.
 */
class TrilinearInterpolation :
    StatelessStrategy(),
    SpatioTemporalInterpolation by SeparableSpatioTemporalInterpolation(
        BilinearInterpolation(),
        LinearInterpolation(),
    )
