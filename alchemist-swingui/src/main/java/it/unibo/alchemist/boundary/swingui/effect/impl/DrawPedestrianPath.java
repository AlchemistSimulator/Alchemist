/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the paths took by pedestrians.
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DrawPedestrianPath extends AbstractDrawOnce {

    /**
     */
    protected static final int MAX_COLOUR_VALUE = 255;
    /**
     */
    protected static final int INITIAL_ALPHA_DIVIDER = 3;
    /**
     */
    protected static final Logger L = LoggerFactory.getLogger(DrawShape.class);
    /**
     * The paths will be drawn as circles of this diameter.
     */
    protected static final int DIAMETER = 5;
    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "A")
    private RangedInteger alpha = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE / INITIAL_ALPHA_DIVIDER);
    @ExportForGUI(nameToExport = "R")
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    private Color colorCache = Color.BLUE;
    @ExportForGUI(nameToExport = "to be drawn")
    private boolean toBeDrawn = true;
    private final List<Position2D> path = new ArrayList<>();

    /**
     * @param graphics2D        graphics
     * @param node        node
     * @param environment      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     * @param <T>      concentration type
     * @param <P>      position type
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    protected <T, P extends Position2D<P>> void draw(
            final Graphics2D graphics2D,
            final Node<T> node,
            final Environment<T, P> environment,
            final Wormhole2D<P> wormhole
    ) {
        path.add(environment.getPosition(node));
        if (toBeDrawn) {
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            graphics2D.setColor(colorCache);
            path.forEach(p -> {
                final Point viewP = ((Wormhole2D<Position2D<?>>) wormhole).getViewPoint(p);
                graphics2D.fillOval(viewP.x, viewP.y, DIAMETER, DIAMETER);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    /**
     * @return alpha channel
     */
    public RangedInteger getAlpha() {
        return alpha;
    }

    /**
     * @param alpha alpha channel
     */
    public void setAlpha(final RangedInteger alpha) {
        this.alpha = alpha;
    }

    /**
     * @return red channel
     */
    public RangedInteger getRed() {
        return red;
    }

    /**
     * @param red red channel
     */
    public void setRed(final RangedInteger red) {
        this.red = red;
    }

    /**
     * @return green channel
     */
    public RangedInteger getGreen() {
        return green;
    }

    /**
     * @param green green channel
     */
    public void setGreen(final RangedInteger green) {
        this.green = green;
    }

    /**
     * @return blue channel
     */
    public RangedInteger getBlue() {
        return blue;
    }

    /**
     * @param blue blue channel
     */
    public void setBlue(final RangedInteger blue) {
        this.blue = blue;
    }

    /**
     * @return if it is to be drawn
     */
    public boolean isToBeDrawn() {
        return toBeDrawn;
    }

    /**
     * @param toBeDrawn if it is to be drawn
     */
    public void setToBeDrawn(final boolean toBeDrawn) {
        this.toBeDrawn = toBeDrawn;
    }
}
