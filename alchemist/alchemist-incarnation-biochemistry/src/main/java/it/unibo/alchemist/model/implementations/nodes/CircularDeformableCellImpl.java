/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Implementation of a circular deformable cell.
 *
 */
public class CircularDeformableCellImpl<P extends Position<P>> extends CellNodeImpl<P> implements CircularDeformableCell<P> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final double maxDiam;

    /**
     * Create a circular deformable cell of maxDiam = maxDiameter and minDiam = deformability * maxDiam.
     * @param env 
     * @param maxDiameter 
     * @param rigidity 
     */
    public CircularDeformableCellImpl(final Environment<Double, P> env, final double maxDiameter, final double rigidity) {
        super(env, maxDiameter * rigidity);
        if (rigidity > 1 || rigidity < 0) {
            throw new IllegalArgumentException("deformability must be between 0 and 1");
        }
        this.maxDiam = maxDiameter;
    }


    @Override
    public double getMaxDiameter() {
        return maxDiam;
    }

    @Override
    public double getMaxRadius() {
        return maxDiam / 2;
    }
}
