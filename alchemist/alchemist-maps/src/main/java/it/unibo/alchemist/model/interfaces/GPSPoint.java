/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 */
public interface GPSPoint extends Serializable, Comparable<GPSPoint> {

    /**
     * @return the latitude
     */
    double getLatitude();

    /**
     * @return the longitude
     */
    double getLongitude();

    /**
     * @return the time
     */
    double getTime();

    /**
     * @param t
     *            time to set
     */
    void setTime(double t);

    /**
     * @return a new {@link Position} representation of this {@link GPSPoint}
     */
    Position toPosition();

}
