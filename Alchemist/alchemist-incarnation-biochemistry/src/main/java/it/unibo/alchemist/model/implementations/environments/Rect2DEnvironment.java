/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors

 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.interfaces.Position;

/**
 */
public class Rect2DEnvironment extends LimitedContinuos2D<Double> {

    private static final long serialVersionUID = -2952112972706738682L;

    @Override
    protected Position next(final double ox, final double oy, final double nx, final double ny) {
        throw new UnsupportedOperationException("next() has to be implemented in: " + getClass());
    }

    @Override
    protected boolean isAllowed(final Position p) {
        return true;
        //throw new UnsupportedOperationException("isAllowed(Position) has to be implemented in: " + getClass());
    }
}