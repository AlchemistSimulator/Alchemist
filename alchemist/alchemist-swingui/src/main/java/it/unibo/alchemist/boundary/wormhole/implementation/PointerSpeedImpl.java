/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import java.awt.Point;

import it.unibo.alchemist.boundary.wormhole.interfaces.PointerSpeed;

/**
 * Implementation for {@link PointerSpeed} interface.
 * 
 */
public class PointerSpeedImpl implements PointerSpeed {
    private Point oldPosition = new Point();
    private Point position = new Point();

    @Override
    public Point getCurrentPosition() {
        return (Point) position.clone();
    }

    @Override
    public Point getOldPosition() {
        return (Point) oldPosition.clone();
    }

    @Override
    public Point getVariation() {
        return new Point(
                (int) (position.getX() - oldPosition.getX()),
                (int) (position.getY() - oldPosition.getY()));
    }

    @Override
    public void setCurrentPosition(final Point point) {
        oldPosition = position;
        position = (Point) point.clone();
    }

}
