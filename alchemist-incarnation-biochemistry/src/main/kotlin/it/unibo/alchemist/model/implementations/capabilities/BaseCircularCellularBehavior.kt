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
import it.unibo.alchemist.model.interfaces.capabilities.CellularBehavior
import it.unibo.alchemist.model.interfaces.capabilities.CircularCellularBehavior

/**
 * Base implementation of a [CircularCellularBehavior].
 */
class BaseCircularCellularBehavior<P : Position<P>> @JvmOverloads constructor(
    environment: Environment<Double, P>,
    override val node: Node<Double>,
    override val diameter: Double = 0.0,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap()
) : CircularCellularBehavior<P>,
    CellularBehavior<P> by BaseCellularBehavior(environment, node, junctions)
