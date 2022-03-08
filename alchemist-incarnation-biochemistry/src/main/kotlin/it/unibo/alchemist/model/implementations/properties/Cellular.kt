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
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.properties.CellularProperty
import org.apache.commons.math3.util.FastMath

/**
 * Base implementation of a [CellularProperty].
 */
class Cellular<P : Position<P>> @JvmOverloads constructor(
    /**
     * The environment in which [node] is moving.
     */
    val environment: Environment<Double, P>,
    override val node: Node<Double>,
    override val junctions: MutableMap<Junction, MutableMap<Node<Double>, Int>> = LinkedHashMap(),
) : CellularProperty<P> {

    override var polarizationVersor: P = environment.makePosition(0, 0)

    override fun addPolarizationVersor(versor: P) {
        val tempCor = (polarizationVersor + versor.coordinates).coordinates
        val module = FastMath.hypot(tempCor[0], tempCor[1])
        polarizationVersor = if (module == 0.0) environment.makePosition(0, 0) else environment.makePosition(
            tempCor[0] / module,
            tempCor[1] / module
        )
    }
}
