/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

/**
 * {@link Environment} supporting deformable cells.
 *
 * @param <P> position type
 */
public interface EnvironmentSupportingDeformableCells<P extends Position<? extends P>> extends Environment<Double, P> {

    /**
     * 
     * @return the biggest among the deformable cell's diameter, when not stressed. 
     */
    double getMaxDiameterAmongCircularDeformableCells();
}
