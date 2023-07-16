/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps.routes;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.maps.positions.GPSPointImpl;
import org.junit.jupiter.api.Test;

/**
 *
 */
class TestGPSTraceImpl {

    /**
     * 
     */
    @Test
    void testConstructionWithList() {
        new GPSTraceImpl(ImmutableList.of(new GPSPointImpl(0d, 0d, Time.ZERO)));
    }

}
