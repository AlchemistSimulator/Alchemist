/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

/**
 * Implements a circular deformable cell.
 *
 * @param <P>
 *
 */
public interface CircularDeformableCell<P extends Position<? extends P>> extends CellWithCircularArea<P> {

    /**
     * 
     * @return the max diameter that this cell can have, e.g. the diameter that this cell has if no other cell is around.
     */
    double getMaxDiameter();

    /**
     * 
     * @return the max radius that this cell can have, e.g. the radius that this cell has if no other cell is around.
     */
    double getMaxRadius();
}
