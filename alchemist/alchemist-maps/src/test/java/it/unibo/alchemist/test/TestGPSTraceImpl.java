/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;

/**
 *
 */
public class TestGPSTraceImpl {

    /**
     * 
     */
    @Test
    public void testConstructionWithList() {
        assertNotNull(new GPSTraceImpl(ImmutableList.of(
                new GPSPointImpl(0d,  0d, DoubleTime.ZERO_TIME))));
    }

}
