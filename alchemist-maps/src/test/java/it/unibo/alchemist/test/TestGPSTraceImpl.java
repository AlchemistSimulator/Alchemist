/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.Time;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
class TestGPSTraceImpl {

    /**
     * 
     */
    @Test
    void testConstructionWithList() {
        assertNotNull(new GPSTraceImpl(ImmutableList.of(
                new GPSPointImpl(0d,  0d, Time.ZERO))));
    }

}
