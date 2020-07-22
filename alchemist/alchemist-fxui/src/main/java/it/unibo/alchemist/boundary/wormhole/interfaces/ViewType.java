/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.interfaces;

/**
 * This interface implements an Adapter pattern between a generic view element and the needs of a {@link Wormhole2D}.
 */
public interface ViewType {

    /**
     * Getter method for the width of the adapted view.
     *
     * @return the width
     */
    double getWidth();

    /**
     * Getter method for the height of the adapted view.
     *
     * @return the height
     */
    double getHeight();
}
