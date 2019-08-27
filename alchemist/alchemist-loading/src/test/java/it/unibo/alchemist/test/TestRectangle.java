/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.loader.shapes.Rectangle;
import it.unibo.alchemist.loader.shapes.Shape;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class TestRectangle {

    /**
     * 
     */
    @Test
    public void test() {
        final Shape<GeoPosition> s = new Rectangle<>(12, 44, 1, 1);
        // CHECKSTYLE: MagicNumber OFF
        assertTrue(s.contains(new LatLongPosition(44.132300, 12.233000)));
        // CHECKSTYLE: MagicNumber ON
    }

    /**
     *
     */
    @Test
    public void rectangleWithNegativeDimension() {
        /*
         * In this rectangle the x should go from 10 to 15 and the y from 45 to 35
         */
        final Shape<Euclidean2DPosition> s = new Rectangle<>(15, 45, -5, -10);
        // CHECKSTYLE: MagicNumber OFF
        assertTrue(s.contains(new Euclidean2DPosition(12, 40)));
        // CHECKSTYLE: MagicNumber ON
    }

}
