/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import java.io.File

/**
 * A [CSVAlchemistSimulationAdapter] that is used to load a simulation result file
 * of the experiment about the Space-Fluid Adaptive Sampling algorithm.
 * @see <a href="https://github.com/DanySK/Experiment-2022-Coordination-Space-Fluid">
 *      Experiment-2022-Coordination-Space-Fluid
 *     </a>
 * @see <a href="https://link.springer.com/chapter/10.1007/978-3-031-08143-9_7">
 *      Space-Fluid Adaptive Sampling: A Field-Based, Self-organising Approach
 *     </a>
 * @param gridType the type of grid used in the experiment
 * @param seed the seed used to select the seed to use for the simulation,
 *             i.e. the seed given from MultiVesta
 * @param rootOutputFiles the root folder where the CSV files are stored
 */
class ExperimentSimulationAdapter(
    /**
     * The type of grid used in the experiment.
     */
    val gridType: GridType,
    seed: Int,
    rootOutputFiles: File = File("data")
) : CSVAlchemistSimulationAdapter(seed, rootOutputFiles, { "coord_deployment-${gridType}_seed-$it.0.csv" }) {

    /**
     * The type of grid used in the experiment.
     */
    enum class GridType(
        /**
         * The short name of the grid type, as used in the experiment.
         */
        private val shortName: String
    ) {
        /**
         * The regular grid.
         */
        REGULAR("grid"),
        /**
         * The perturbed (or irregular) grid.
         */
        PERTURBED("pgrid"),
        /**
         * The random grid.
         */
        UNIFORM("uniform"),
        /**
         * The grid uniform on an axis and exponential on the other.
         */
        EXPONENTIAL("exp");

        override fun toString(): String {
            return shortName
        }
    }
}
