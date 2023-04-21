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
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.properties.CircularCellProperty
import it.unibo.alchemist.model.interfaces.properties.CircularDeformableCellProperty

/**
 * Base implementation of a [CircularCellProperty].
 */
class CircularDeformableCell @JvmOverloads constructor(
    environment: Environment<Double, Euclidean2DPosition>,
    override val node: Node<Double>,
    override val maximumDiameter: Double,
    override val rigidity: Double,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : AbstractNodeProperty<Double>(node),
    CircularDeformableCellProperty,
    CircularCellProperty by CircularCell(
        environment,
        node,
        maximumDiameter * rigidity,
        junctions,
    ) {
    init {
        require(rigidity in 0.0..1.0) {
            "deformability must be between 0 and 1"
        }
    }

    override fun toString() = "${super.toString()}[maximumDiameter=$maximumDiameter, rigidity=$rigidity]"
}
