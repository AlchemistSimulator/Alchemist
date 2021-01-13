/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition.DistanceFormula;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class TestLatLongPosition {

    private static final LatLng NORTH_CAPE = new LatLng(71.172_5, 25.784_444);
    private static final LatLng INVERCAGILL = new LatLng(-46.412_652, 168.368_963);

    /**
     * 
     */
    @Test
    public void testDistance() {
        for (final DistanceFormula df1 : DistanceFormula.values()) {
            final double dist = LatLongPosition.distance(NORTH_CAPE, INVERCAGILL, df1);
            final double dist2 = LatLongPosition.distance(INVERCAGILL, NORTH_CAPE, df1);
            assertEquals(dist, dist2, Double.MIN_VALUE);
            assertEquals(dist, LatLongPosition.distance(NORTH_CAPE, INVERCAGILL, LengthUnit.METER, df1), Double.MIN_VALUE);
            assertEquals(dist / LengthUnit.METER.getScaleFactor(),
                    LatLongPosition.distance(NORTH_CAPE, INVERCAGILL, LengthUnit.KILOMETER, df1), Double.MIN_VALUE);
        }
    }

}
