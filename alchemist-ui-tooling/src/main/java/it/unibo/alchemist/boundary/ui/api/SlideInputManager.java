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
 * <code>ISlideInputManager</code> is the base type for any class whose aim is
 * to handle the sliding of any physical/virtual device/control.
 *
 */
public interface SlideInputManager {
    /**
     * Decreases the total number of slides.
     *
     * @param value
     *            the number of slides
     */
    void dec(double value);

    /**
     * Increases the total number of slides.
     *
     * @param value
     *            the number of slides
     */
    void inc(double value);
}
