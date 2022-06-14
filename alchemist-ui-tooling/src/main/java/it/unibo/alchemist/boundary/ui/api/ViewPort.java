/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.ui.api;

/**
 * This interface implements an Adapter pattern between a generic view element and the needs of a {@link Wormhole2D}.
 */
public interface ViewPort {

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
