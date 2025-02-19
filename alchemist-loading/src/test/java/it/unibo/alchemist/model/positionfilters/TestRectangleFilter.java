/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positionfilters;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.PositionBasedFilter;
import it.unibo.alchemist.model.maps.positions.LatLongPosition;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
class TestRectangleFilter {

    /**
     *
     */
    @Test
    void test() {
        final PositionBasedFilter<GeoPosition> s = new Rectangle<>(12, 44, 1, 1);
        // CHECKSTYLE: MagicNumber OFF
        assertTrue(s.contains(new LatLongPosition(44.132300, 12.233000))); // NOPMD
        // CHECKSTYLE: MagicNumber ON
    }

    /**
     *
     */
    @Test
    void rectangleWithNegativeDimension() {
        /*
         * In this rectangle the x should go from 10 to 15 and the y from 45 to 35
         */
        final PositionBasedFilter<Euclidean2DPosition> s = new Rectangle<>(15, 45, -5, -10);
        // CHECKSTYLE: MagicNumber OFF
        assertTrue(s.contains(new Euclidean2DPosition(12, 40)));
        // CHECKSTYLE: MagicNumber ON
    }

}
