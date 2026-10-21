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
 * A geographic position associated with a time, as recorded by a GPS device.
 */
public interface GPSPoint extends GeoPosition, Comparable<GPSPoint> {

    /**
     * Returns the time at which this point was recorded.
     *
     * @return the time
     */
    Time getTime();

    /**
     * Returns a copy of this point with the given time subtracted from its time.
     *
     * @param t time to subtract to the point's time
     * @return new GPSPoint with the new time
     */
    GPSPoint subtractTime(Time t);

    /**
     * Returns a copy of this point with the given time added to its time.
     *
     * @param t time to add to the point's time
     * @return new GPSPoint with the new time
     */
    GPSPoint addTime(Time t);
}
