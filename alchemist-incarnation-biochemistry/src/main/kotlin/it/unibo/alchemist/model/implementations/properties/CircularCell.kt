/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.implementations.molecules.Junction
import it.unibo.alchemist.model.interfaces.properties.CellProperty
import it.unibo.alchemist.model.interfaces.properties.CircularCellProperty
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.properties.AbstractNodeProperty

/**
 * Base implementation of a [CircularCellProperty].
 */
class CircularCell @JvmOverloads constructor(
    environment: Environment<Double, Euclidean2DPosition>,
    override val node: Node<Double>,
    override val diameter: Double = 0.0,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : AbstractNodeProperty<Double>(node),
    CircularCellProperty,
    CellProperty<Euclidean2DPosition> by Cell(environment, node, junctions) {

    override fun toString() = "${super.toString()}[diameter=$diameter]"
}
