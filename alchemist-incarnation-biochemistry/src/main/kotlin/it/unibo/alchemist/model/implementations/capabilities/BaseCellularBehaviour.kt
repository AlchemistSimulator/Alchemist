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
import it.unibo.alchemist.model.interfaces.capabilities.CellularBehaviour

/**
 * Base implementation of a [CellularBehaviour].
 */
class BaseCellularBehaviour<P : Position<P>>(
    environment: Environment<Double, P>,
    override val node: Node<Double>,
    override val diameter: Double,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : CellularBehaviour<P> {

    override var polarizationVersor: P = environment.makePosition(0, 0)

    override fun addPolarizationVersor(versor: P) { polarizationVersor += versor.coordinates }
}
