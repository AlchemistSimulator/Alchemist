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
 * A class that implements the <code>IAngleManager</code> interface is able to
 * convert the sliding of any physical/virtual device/control into a positive
 * <code>double</code> value that represents an angle.
 * 

 */
public interface AngleManager extends ISlideInputManager {
    /**
     * Gets the angle.
     * 
     * @return a <code>double</code> value representing an angle in radians
     */
    double getAngle();
}
