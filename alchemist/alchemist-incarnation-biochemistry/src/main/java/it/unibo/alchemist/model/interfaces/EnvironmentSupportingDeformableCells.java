/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.interfaces;

/**
 * {@link Environment} supporting deformable cells.
 */
public interface EnvironmentSupportingDeformableCells<P extends Position<? extends P>> extends Environment<Double, P> {

    /**
     * 
     * @return the biggest among the deformable cell's diameter, when not stressed. 
     */
    double getMaxDiameterAmongCircularDeformableCells();
}
