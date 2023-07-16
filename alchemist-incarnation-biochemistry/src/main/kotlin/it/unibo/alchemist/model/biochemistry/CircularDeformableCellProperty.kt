/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry

/**
 * A node's capability to behave as a circular deformable cell.
 */
interface CircularDeformableCellProperty : CircularCellProperty {
    /**
     *
     * The max diameter that this cell can have, e.g. the diameter that this cell has if no other cell is around.
     */
    val maximumDiameter: Double

    /**
     *
     * The max radius that this cell can have, e.g. the radius that this cell has if no other cell is around.
     */
    val maximumRadius: Double
        get() = maximumDiameter / 2

    /**
     * Cellular rigidity.
     */
    val rigidity: Double
}
