/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;


/**
 */
@Deprecated
public interface Effect extends Serializable {

    /**
     * Applies the effect.
     * 
     * @param graphic
     *            Graphics2D to use
     * @param node
     *            the node to draw
     * @param x
     *            x screen position
     * @param y
     *            y screen position
     * @deprecated use {@link #apply(Graphics2D, Node, Environment, BidimensionalWormhole)} instead.
     */
    @Deprecated
    default void apply(Graphics2D graphic, Node<?> node, int x, int y) {
        // deprecated, defaults to nothing.
    }

    /**
     * Applies the effect.
     *
     * @param <T>      concentration type
     * @param <P>      position type
     * @param g        graphics
     * @param n        node
     * @param env      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     */
    @SuppressWarnings("deprecation")
    default <T, P extends Position2D<P>> void apply(Graphics2D g, Node<T> n, Environment<T, P> env, BidimensionalWormhole<P> wormhole) {
        final Point viewPoint = wormhole.getViewPoint(env.getPosition(n));
        apply(g, n, viewPoint.x, viewPoint.y); // preserve backward compatibility
    }

    /**
     * @return a color which resembles the color of this effect
     */
    Color getColorSummary();

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
