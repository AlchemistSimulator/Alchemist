/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.properties

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import org.apache.commons.math3.util.FastMath
import kotlin.math.nextDown
import kotlin.math.nextUp

/**
 * Base implementation of a [CellProperty].
 */
class Cell
    @JvmOverloads
    constructor(
        /**
         * The environment in which [node] is moving.
         */
        val environment: Environment<Double, Euclidean2DPosition>,
        override val node: Node<Double>,
        override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
    ) : AbstractNodeProperty<Double>(node), CellProperty<Euclidean2DPosition> {
        override var polarizationVersor: Euclidean2DPosition = Euclidean2DPosition.zero

        override fun addPolarizationVersor(versor: Euclidean2DPosition) {
            val tempCor = (polarizationVersor + versor.coordinates).coordinates
            val module = FastMath.hypot(tempCor[0], tempCor[1])
            polarizationVersor =
                when (module) {
                    in 0.0.nextDown()..0.0.nextUp() -> Euclidean2DPosition.zero
                    else -> Euclidean2DPosition(tempCor[0] / module, tempCor[1] / module)
                }
        }

        override fun cloneOnNewNode(node: Node<Double>) = Cell(environment, node)
    }
