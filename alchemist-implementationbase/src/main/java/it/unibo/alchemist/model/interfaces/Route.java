/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces;

import it.unibo.alchemist.model.Position;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

/**
 * @param <P> type of Position followed by {@link Route} 
 */
public interface Route<P extends Position<?>> extends Iterable<P>, Serializable {

    /**
     * @return the length of the route
     */
    double length();

    /**
     * @param step
     *            the step
     * @return the step-th {@link Position} in the route
     */
    P getPoint(int step);

    /**
     * @return the route as list of {@link Position}
     */
    List<P> getPoints();

    /**
     * 
     * @return the route as stream of {@link Position}
     */
    Stream<P> stream();

    /**
     * @return the number of points this route is made of
     */
    int size();

}
