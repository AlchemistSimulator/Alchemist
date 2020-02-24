/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.*;
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph;
import it.unibo.alchemist.model.interfaces.geometry.graph.OrientingAgent;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

/**
 */
public class DrawCognitiveMap implements Effect {

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
    private RangedInteger red = new RangedInteger(0, MAX_COLOUR_VALUE, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "G")
    private RangedInteger green = new RangedInteger(0, MAX_COLOUR_VALUE);
    @ExportForGUI(nameToExport = "B")
    private RangedInteger blue = new RangedInteger(0, MAX_COLOUR_VALUE);
    private Color colorCache = Color.RED;
    private NavigationGraph<? extends Euclidean2DPosition, ?, Ellipse, ?> cognitiveMap = null;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Optional<Node> markerNode = Optional.empty();

    /**
     * @param g        graphics
     * @param n        node
     * @param env      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     * @param <T>      concentration type
     * @param <P>      position type
     */
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "unchecked", "checkstyle:WhitespaceAfter"})
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public <T, P extends Position2D<P>> void apply(final Graphics2D g, final Node<T> n, final Environment<T, P> env, final IWormhole2D<P> wormhole) {
        // if marker node is no longer in the environment or it is no longer displayed, we need to change it
        if (markerNode.isPresent()
                && (!env.getNodes().contains(markerNode.get()) || !wormhole.isInsideView(wormhole.getViewPoint(env.getPosition((Node<T>) markerNode.get()))))) {
            markerNode = Optional.empty();
        }
        if (markerNode.isEmpty()) {
            markerNode = Optional.of(n);
        }
        if (markerNode.get() == n && cognitiveMap != null) { // at this point markerNode.isPresent() is always true, so we directly get it
            final IWormhole2D<Euclidean2DPosition> w = (IWormhole2D<Euclidean2DPosition>) wormhole;
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            g.setColor(Color.RED);
            cognitiveMap.nodes().stream()
                    .map(r -> mapEnvEllipseToAwtShape(r, w))
                    .forEach(r -> {
                        g.setColor(colorCache);
                        g.fill(r);
                        g.setColor(colorCache.brighter().brighter());
                        g.draw(r);
                    });
            cognitiveMap.nodes().forEach(r -> {
                final Point centroidFrom = w.getViewPoint(r.getCentroid());
                //g.setColor(colorCache);
                //g.fillOval(centroidFrom.x, centroidFrom.y, 10, 10);
                cognitiveMap.edgesFrom(r).forEach(e -> {
                    final Point centroidTo = w.getViewPoint(e.getTo().getCentroid());
                    g.setColor(colorCache);
                    g.drawLine(centroidFrom.x, centroidFrom.y, centroidTo.x, centroidTo.y);
                });
            });
        }
        if (cognitiveMap == null && markerNode.get() instanceof OrientingAgent
                && env instanceof Environment2DWithObstacles
                && env.makePosition(0.0, 0.0) instanceof Euclidean2DPosition) {
            OrientingAgent<? extends Euclidean2DPosition, ?, Ellipse, ?> p = (OrientingAgent) markerNode.get();
            this.cognitiveMap = p.getCognitiveMap();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    private Shape mapEnvEllipseToAwtShape(final Ellipse e, final IWormhole2D<Euclidean2DPosition> wormhole) {
        final Rectangle2D frame = e.asAwtShape().getFrame();
        final Euclidean2DPosition startEnv = new Euclidean2DPosition(frame.getMinX(), frame.getMinY());
        final Euclidean2DPosition endEnv = new Euclidean2DPosition(frame.getMaxX(), frame.getMaxY());
        final Point startView = wormhole.getViewPoint(startEnv);
        final Point endView = wormhole.getViewPoint(endEnv);
        final Point2D minPoint = new Point2D.Double(Math.min(startView.getX(), endView.getX()), Math.min(startView.getY(), endView.getY()));
        final Point2D maxPoint = new Point2D.Double(Math.max(startView.getX(), endView.getX()), Math.max(startView.getY(), endView.getY()));
        return new Ellipse2D.Double(minPoint.getX(), minPoint.getY(), maxPoint.getX() - minPoint.getX(), maxPoint.getY() - minPoint.getY());
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
