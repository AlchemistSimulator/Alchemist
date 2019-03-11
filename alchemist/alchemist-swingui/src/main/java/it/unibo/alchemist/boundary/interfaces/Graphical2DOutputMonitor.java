/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * An output monitor that supports zooming on bidimensional environments.
 * 
 * @param <T>
 */
public interface Graphical2DOutputMonitor<T, P extends Position<? extends P>> extends GraphicalOutputMonitor<T, P> {

    /**
     * @param center
     *            the point where to zoom
     * @param zoomLevel
     *            the desired zoom level
     */
    void zoomTo(P center, double zoomLevel);

}
