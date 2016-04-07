/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.interfaces;

/**
 * <code>ISlideInputManager</code> is the base type for any class whose aim is
 * to handle the the sliding of any physical/virtual device/control.
 * 

 */
public interface ISlideInputManager {
    /**
     * Decreases the total amount of slides.
     * 
     * @param value
     *            is the number of slides
     */
    void dec(double value);

    /**
     * Increases the total amount of slides.
     * 
     * @param value
     *            is the number of slides
     */
    void inc(double value);
}
