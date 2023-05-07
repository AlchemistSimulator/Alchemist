/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Time;

/**
 */
public interface GPSTrace extends TimedRoute<GPSPoint> {

    /**
     * @param time
     *            the time
     * @return the next point
     */
    GPSPoint getNextPosition(Time time);

    /**
     * @param time
     *            the time
     * @return the previous point
     */
    GPSPoint getPreviousPosition(Time time);

    /**
     * @return the first time for this {@link GPSTrace}
     */
    Time getStartTime();

    /**
     * @return the final time for this {@link GPSTrace}
     */
    Time getFinalTime();

    /**
     * @param time
     *            the time
     * @return the position at which the node would have been if it has moved in
     *         a straight line connecting the previous and the next point of
     *         time at constant speed
     */
    GeoPosition interpolate(Time time);

    /**
     * @param time
     *            the time at which the new trace should start
     * @return a new trace, which will have all the points of this trace
     *         starting at the passed time
     */
    GPSTrace startAt(Time time);

    /**
     * 
     * @return The initial position of the trace
     */
    GPSPoint getInitialPosition();

    /**
     * 
     * @return The final position of the trace
     */
    GPSPoint getFinalPosition();
}
