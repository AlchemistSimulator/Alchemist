/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import it.unibo.alchemist.common.model.surrogate.GeneralPositionSurrogate
import it.unibo.alchemist.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D

/**
 * A set of functions to map the Position to the corresponding surrogate classes.
 */
object ToPositionSurrogate {

    /**
     * @return the most suitable function to maps the [it.unibo.alchemist.model.interfaces.Position].
     */
    fun toSuitablePositionSurrogate(dimensions: Int): (Position<*>) -> PositionSurrogate = when (dimensions) {
        2 -> toPosition2DSurrogate
        else -> toGeneralPositionSurrogate
    }

    /**
     * @return A function that maps a [it.unibo.alchemist.model.interfaces.Position] to a
     * [it.unibo.alchemist.model.surrogate.Position2DSurrogate] surrogate class. This works only
     * if the position is a [it.unibo.alchemist.model.interfaces.Position2D].
     */
    private val toPosition2DSurrogate: (Position<*>) -> Position2DSurrogate = { position ->
        require(position is Position2D<*>)
        Position2DSurrogate(position.x, position.y)
    }

    /**
     * @return A function that maps a [it.unibo.alchemist.model.interfaces.Position] to its surrogate class.
     */
    private val toGeneralPositionSurrogate: (Position<*>) -> GeneralPositionSurrogate = { position ->
        GeneralPositionSurrogate(position.coordinates, position.dimensions)
    }
}
