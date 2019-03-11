/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.shapes;

import java.awt.geom.Rectangle2D;

/**
 * A Rectangle.
 */
public class Rectangle extends Abstract2DShape {

    /**
     * @param x
     *            start x point
     * @param y
     *            start y point
     * @param w
     *            width
     * @param h
     *            height
     */
    public Rectangle(final double x, final double y, final double w, final double h) {
        super(new Rectangle2D.Double(x, y, w, h));
    }

}
