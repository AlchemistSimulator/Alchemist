/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.positions;

/**
 * 
 */
public class Continuous2DEuclidean extends ContinuousGenericEuclidean {

    private static final long serialVersionUID = 1042391992665398942L;

    /**
     * @param xp
     *            The X coordinate
     * @param yp
     *            The Y coordinate
     */
    public Continuous2DEuclidean(final double xp, final double yp) {
        super(xp, yp);
    }

    /**
     * @param c an array of length 2 containing the coordinates
     */
    public Continuous2DEuclidean(final double[] c) {
        super(c);
        if (c.length != 2) {
            throw new IllegalArgumentException("The array must have exactly two elements.");
        }
    }

}
