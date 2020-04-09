/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.implementations.environments.ImageEnvironmentWithGraph;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon;
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D;
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage;
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * Draws the navigation graph of an {@link ImageEnvironmentWithGraph}.
 */
public class DrawNavigationGraph extends DrawOnce {

    /**
     *
     */
    protected static final int MAX_COLOUR_VALUE = 255;
    /**
     *
     */
    protected static final int INITIAL_ALPHA_DIVIDER = 5;
    /**
     *
     */
    protected static final Logger L = LoggerFactory.getLogger(DrawShape.class);
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
    @Nullable
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient NavigationGraph<Euclidean2DPosition, ?, ConvexPolygon, Euclidean2DPassage> graph;

    /**
     * @param g        graphics
     * @param n        node
     * @param env      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     * @param <T>      concentration type
     * @param <P>      position type
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    protected <T, P extends Position2D<P>> void draw(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        if (graph == null && env instanceof ImageEnvironmentWithGraph) {
            graph = ((ImageEnvironmentWithGraph<T>) env).graph();
        }
        if (graph != null) {
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            graph.vertexSet().stream()
                    .map(r -> mapEnvConvexPolygonToAwtShape(r, wormhole, env))
                    .forEach(r -> {
                        g.setColor(colorCache);
                        g.fill(r);
                        g.setColor(colorCache.brighter().brighter());
                        g.draw(r);
                    });
            graph.destinations().forEach(destination -> {
                final Point viewPoint = wormhole.getViewPoint((P) destination);
                g.setColor(Color.GREEN);
                g.fillOval(viewPoint.x, viewPoint.y, 10, 10);
            });
            graph.vertexSet().forEach(r -> {
                final Point centroidFrom = wormhole.getViewPoint(env.makePosition(r.getCentroid().getX(), r.getCentroid().getY()));
                if (graph != null) {
                    graph.outgoingEdgesOf(r).forEach(e -> {
                        final Segment2D<Euclidean2DPosition> passage = e.getPassageShape();
                        final Point viewP1 = wormhole.getViewPoint(env.makePosition(passage.getFirst().getX(), passage.getFirst().getY()));
                        final Point viewP2 = wormhole.getViewPoint(env.makePosition(passage.getSecond().getX(), passage.getSecond().getY()));
                        g.setColor(Color.GREEN);
                        g.drawLine(viewP1.x, viewP1.y, viewP2.x, viewP2.y);
                        final Point midPoint = new Point((viewP1.x + viewP2.x) / 2, (viewP1.y + viewP2.y) / 2);
                        g.setColor(colorCache);
                        g.drawLine(centroidFrom.x, centroidFrom.y, midPoint.x, midPoint.y);
                    });
                }
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

    private <T, P extends Position2D<P>> Shape mapEnvConvexPolygonToAwtShape(final ConvexPolygon p, final IWormhole2D<P> wormhole, final Environment<T, P> env) {
        final Path2D shape = new Path2D.Double();
        for (int i = 0; i < p.vertices().size(); i++) {
            final Point viewPoint = wormhole.getViewPoint(env.makePosition(p.vertices().get(i).getX(), p.vertices().get(i).getY()));
            if (i == 0) {
                shape.moveTo(viewPoint.getX(), viewPoint.getY());
            } else {
                shape.lineTo(viewPoint.getX(), viewPoint.getY());
            }
        }
        shape.closePath();
        return shape;
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
}
