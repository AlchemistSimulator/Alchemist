/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.GridSnapshots
import it.unibo.alchemist.model.geospatial.strategy.missing.MissingValueStrategy
import it.unibo.alchemist.model.geospatial.strategy.missing.PropagateNull
import it.unibo.alchemist.model.geospatial.strategy.spatial.BilinearInterpolator
import it.unibo.alchemist.model.geospatial.strategy.spatial.SpatialInterpolationStrategy
import it.unibo.alchemist.model.geospatial.strategy.temporal.LinearInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.TemporalInterpolationStrategy
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/**
 * [CopernicusLayer] that returns values as [Double].
 *
 * By default, it uses bilinear interpolation on the grid and propagates the generated null values.
 * Time slices are linearly interpolated by default.
 *
 * @see CopernicusLayer
 */
class DoubleCopernicusLayer : CopernicusLayer<Double> {
    constructor(
        environment: Environment<*, GeoPosition>,
        data: GridSnapshots<Double>,
        timeOrigin: Instant? = null,
        timeScale: Duration = Duration.ofHours(1),
        spatialInterpolation: SpatialInterpolationStrategy<Double> = BilinearInterpolator(),
        missingValue: MissingValueStrategy<Double> = PropagateNull(),
        temporalInterpolation: TemporalInterpolationStrategy<Double> = LinearInterpolation(),
    ) : super(
        environment,
        data,
        timeOrigin,
        timeScale,
        spatialInterpolation,
        missingValue,
        temporalInterpolation,
    )

    /**
     * Create a [DoubleCopernicusLayer] from a directory of homogeneous data.
     * By default, the raw values in the files are taken "as is" and interpreted as
     * pure [Double] values.
     *
     * @see CopernicusLayer
     */
    constructor(
        environment: Environment<*, GeoPosition>,
        dataDirectory: Path,
        variable: String? = null,
        timeOrigin: Instant? = null,
        timeScale: Duration = Duration.ofHours(1),
        spatialInterpolation: SpatialInterpolationStrategy<Double> = BilinearInterpolator(),
        missingValue: MissingValueStrategy<Double> = PropagateNull(),
        temporalInterpolation: TemporalInterpolationStrategy<Double> = LinearInterpolation(),
        measurementConverter: (Double) -> Double = { it },
    ) : super(
        environment,
        dataDirectory,
        variable,
        timeOrigin,
        timeScale,
        spatialInterpolation,
        missingValue,
        temporalInterpolation,
        measurementConverter,
    )
}
