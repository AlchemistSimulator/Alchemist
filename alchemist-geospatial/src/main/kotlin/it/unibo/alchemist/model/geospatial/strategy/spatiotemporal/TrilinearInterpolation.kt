/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.spatiotemporal

import it.unibo.alchemist.model.geospatial.strategy.spatial.BilinearInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.LinearInterpolation

/**
 * A [SpatioTemporalInterpolation] strategy that performs a trilinear interpolation
 * over the 8-point spatio-temporal cube bounding the requested position and time.
 *
 * Trilinear interpolation is separable: it is exactly equivalent to two bilinear spatial
 * interpolations, blended linearly across time.
 *
 * If any of the 8 bounding corners is missing (represented as [Double.NaN]),
 * the interpolation yields [Double.NaN].
 */
class TrilinearInterpolation :
    SpatioTemporalInterpolation by SeparableSpatioTemporalInterpolation(BilinearInterpolation(), LinearInterpolation())
