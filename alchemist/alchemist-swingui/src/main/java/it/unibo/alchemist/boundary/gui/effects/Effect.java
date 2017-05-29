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
import javafx.scene.input.DataFormat;

/**
 */
public interface Effect extends Serializable {
    /** Default DataFormat. */
    DataFormat DATA_FORMAT = new DataFormat(Effect.class.getName());

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
     * Applies the effect.
     * 
     * @param graphic
     *            Graphics2D to use
     * @param environment
     *            the node to draw
     * @param wormhole
     *            the position
     */
    // void apply(Graphics2D graphic, Environment<?> environment, IWormhole2D wormhole);

    /**
     * @return a color which resembles the color of this effect
     */
    Color getColorSummary();

    /**
     * Returns the dataformat of the group. Useful for drag'n'drop in JavaFX
     * GUIs.
     * 
     * @return the dataformat
     */
    DataFormat getDataFormat();

    @Override // Should override hashCode() method
    int hashCode();

    @Override // Should override equals() method
    boolean equals(Object obj);
}
