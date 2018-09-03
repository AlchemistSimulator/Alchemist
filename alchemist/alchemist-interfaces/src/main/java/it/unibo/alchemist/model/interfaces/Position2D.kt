/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces

/**
 * A bidimensional position.
 *
 * @param <P>
</P> */
interface Position2D<P : Position2D<P>> : Position<P> {
    /**
     * @return horizontal position
     */
    val x: Double
    /**
     * @return vertical position
     */
    val y: Double
}
