/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;
import java.util.List;

/**
 */
public interface IRoute extends Serializable {

    /**
     * @return the length of the route
     */
    double getDistance();

    /**
     * @param step
     *            the step
     * @return the step-th {@link Position} in the route
     */
    Position getPoint(int step);

    /**
     * @return the route as list of {@link Position}
     */
    List<Position> getPoints();

    /**
     * @return the number of points this route is made of
     */
    int getPointsNumber();

    /**
     * @return the time required to walk the route
     */
    double getTime();

}
