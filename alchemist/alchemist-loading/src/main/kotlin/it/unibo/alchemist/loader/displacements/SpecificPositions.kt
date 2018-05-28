package it.unibo.alchemist.loader.displacements

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position

open class SpecificPositions(
    environment: Environment<*>,
    vararg positions: Iterable<Number>
) : Displacement {

    private val positions: List<Position> = positions.flatMap { it.map { environment.makePosition(it) } }

    override fun stream() = positions.stream()
}