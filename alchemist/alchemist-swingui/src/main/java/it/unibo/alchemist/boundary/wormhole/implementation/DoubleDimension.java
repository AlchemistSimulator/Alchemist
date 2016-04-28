/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import java.awt.geom.Dimension2D;

/**
 * Implementation of the {@link Dimension2D} abstract class with double
 * precision.
 * 

 */
public class DoubleDimension extends Dimension2D {

    private double width;
    private double height;

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new <code>DoubleDimension</code> instance with both width
     * and height set to zero.
     */
    public DoubleDimension() {
        this(0d, 0d);
    }

    /**
     * Initializes a new <code>DoubleDimension</code> instance using another
     * {@link Dimension2D} object's data.<br>
     * No side effects.
     * 
     * @param d
     *            is the objects used to get the data
     */
    public DoubleDimension(final Dimension2D d) {
        this(d.getWidth(), d.getHeight());
    }

    /**
     * Initializes a new <code>DoubleDimension</code> using raw data.
     * 
     * @param w
     *            is the <code>double</code> containing the width
     * @param h
     *            is the <code>double</code> containing the height
     */
    public DoubleDimension(final double w, final double h) {
        super();
        width = w;
        height = h;
    }

    /**
     * Initializes a new <code>DoubleDimension</code> through an array of
     * numbers.<br>
     * d[0] is width, d[1] is height, other elements will be ignored.
     * 
     * @param d
     *            is a mono-dimensional array of {@link Number}.
     */
    public DoubleDimension(final double[] d) {
        this(d[0], d[1]);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public void setSize(final double w, final double h) {
        width = w;
        height = h;
    }

}
