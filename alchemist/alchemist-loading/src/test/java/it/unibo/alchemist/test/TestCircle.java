/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unibo.alchemist.loader.shapes.Circle;
import it.unibo.alchemist.loader.shapes.Shape;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public class TestCircle {

    private final Shape s = new Circle(0, 0, 1);

    /**
     * 
     */
    @Test
    public void test() {
        assertTrue(check(0, 0));
        assertTrue(check(0.5, 0.5));
        assertTrue(check(-0.5, 0.5));
        assertTrue(check(0.5, -0.5));
        assertTrue(check(-0.5, -0.5));
        assertFalse(check(1, 1));
        assertFalse(check(-1, 1));
        assertFalse(check(1, -1));
        assertFalse(check(-1, -1));
        final double border = Math.sin(Math.PI / 4);
        final double inBorder = Math.nextDown(border);
        final double outBorder = border + 0.00001;
        assertTrue(check(inBorder, inBorder));
        assertTrue(check(-inBorder, inBorder));
        assertTrue(check(inBorder, -inBorder));
        assertTrue(check(-inBorder, -inBorder));
        assertFalse(check(outBorder, outBorder));
        assertFalse(check(-outBorder, outBorder));
        assertFalse(check(outBorder, -outBorder));
        assertFalse(check(-outBorder, -outBorder));
    }

    private static Position mkPos(final double x, final double y) {
        return new LatLongPosition(y, x);
    }

    private boolean check(final double x, final double y) {
        return s.contains(mkPos(x, y));
    }

}
