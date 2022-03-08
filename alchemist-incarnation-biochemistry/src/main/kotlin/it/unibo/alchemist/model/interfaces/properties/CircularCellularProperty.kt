/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.interfaces.Position

/**
 * A node's capability to behave as a cell with a circular area.
 */
interface CircularCellularProperty<P : Position<P>> : CellularProperty<P> {
    /**
     * The diameter of the cell.
     */
    val diameter: Double

    /**
     * The radius of the cell.
     */
    val radius: Double get() = diameter / 2
}
