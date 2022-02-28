/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.molecules.Junction
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.CircularCellularBehavior
import it.unibo.alchemist.model.interfaces.capabilities.CircularDeformableCellularBehavior

/**
 * Base implementation of a [CircularCellularBehavior].
 */
class BaseCircularDeformableCellularBehavior<P : Position<P>> @JvmOverloads constructor(
    environment: Environment<Double, P>,
    override val node: Node<Double>,
    override val maximumDiameter: Double,
    override val rigidity: Double,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : CircularDeformableCellularBehavior<P>,
    CircularCellularBehavior<P> by BaseCircularCellularBehavior(
        environment,
        node,
        maximumDiameter * rigidity,
        junctions
    ) {
    init {
        assert(rigidity in 0.0..1.0) {
            "deformability must be between 0 and 1"
        }
    }
}
