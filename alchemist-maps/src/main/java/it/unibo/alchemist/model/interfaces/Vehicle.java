/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.routing.util.MotorcycleFlagEncoder;
import com.graphhopper.routing.util.MountainBikeFlagEncoder;
import com.graphhopper.routing.util.RacingBikeFlagEncoder;
import com.graphhopper.routing.util.WheelchairFlagEncoder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 */
public enum Vehicle {

    /**
     * Bikes.
     */
    BIKE(new BikeFlagEncoder()),

    /**
     * Cars.
     */
    CAR(new CarFlagEncoder()),

    /**
     * Pedestrians.
     */
    FOOT(new FootFlagEncoder()),

    /**
     * Allows walking along path trails.
     */
    HIKE(new HikeFlagEncoder()),

    /**
     * Mountain Bikes.
     */
    MOUNTAN_BIKE(new MountainBikeFlagEncoder()),

    /**
     * Motorcycles.
     */
    MOTORCYCLE(new MotorcycleFlagEncoder()),

    /**
     * Racing Bikes.
     */
    RACING_BIKE(new RacingBikeFlagEncoder()),

    /**
     * Routes along wheelchair-accessible paths.
     */
    WHEELCHAIR(new WheelchairFlagEncoder());

    private final EncodingManager encoder;

    Vehicle(final FlagEncoder encoder) {
        this.encoder = EncodingManager.start().add(encoder).build();
    }

    /**
     * @return the Graphopper {@link com.graphhopper.routing.util.FlagEncoder}
     * corresponding to the selected vehicle
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "We have little control on GraphHopper mutability")
    public EncodingManager getEncoder() {
        return encoder;
    }
}
