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
public interface GPSTrace extends Serializable {

    /**
     * @param time
     *            the time at which the new trace should start
     * @return a new trace, which will have all the points of this trace
     *         starting at the passed time
     */
    GPSTrace filter(double time);

    /**
     * @return the node id
     */
    int getId();

    /**
     * @param time
     *            the time
     * @return the next point
     */
    GPSPoint getNextPosition(double time);

    /**
     * @param time
     *            the time
     * @return the previous point
     */
    GPSPoint getPreviousPosition(double time);

    /**
     * @return the first time for this {@link GPSTrace}
     */
    double getStartTime();

    /**
     * @param time
     *            the time
     * @return the position at which the node would have been if it has moved in
     *         a straight line connecting the previous and the next point of
     *         time at constant speed
     */
    GPSPoint interpolate(double time);

    /**
     * @return distance (in meters) of the whole trace
     */
    double length();

    /**
     * Modifies all the times of this trace, shifting them back of initialTime.
     * 
     * @param initialTime
     *            the initial time
     */
    void normalizeTimes(double initialTime);

    /**
     * @param i
     *            the id
     */
    void setId(int i);

    /**
     * @return the number of {@link GPSPoint}s
     */
    int size();

    /**
     * orders the trace by time.
     */
    void sort();

}
