/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Node;

/**
 */
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
     */
    void apply(Graphics2D graphic, Node<?> node, int x, int y);

    /**
     * @return a color which resembles the color of this effect
     */
    Color getColorSummary();

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
