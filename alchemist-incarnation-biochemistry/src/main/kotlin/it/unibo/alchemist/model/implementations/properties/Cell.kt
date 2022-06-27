/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.implementations.molecules.Junction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.properties.CellProperty
import org.apache.commons.math3.util.FastMath

/**
 * Base implementation of a [CellProperty].
 */
class Cell @JvmOverloads constructor(
    /**
     * The environment in which [node] is moving.
     */
    val environment: Environment<Double, Euclidean2DPosition>,
    override val node: Node<Double>,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : CellProperty<Euclidean2DPosition> {

    override var polarizationVersor: Euclidean2DPosition = Euclidean2DPosition.zero

    override fun addPolarizationVersor(versor: Euclidean2DPosition) {
        val tempCor = (polarizationVersor + versor.coordinates).coordinates
        val module = FastMath.hypot(tempCor[0], tempCor[1])
        polarizationVersor =
            if (module == 0.0) Euclidean2DPosition.zero
            else Euclidean2DPosition(tempCor[0] / module, tempCor[1] / module)
    }

    override fun cloneOnNewNode(node: Node<Double>) = Cell(environment, node)

    override fun toString() = "Cell${node.id}"
}
