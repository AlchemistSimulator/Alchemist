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
import it.unibo.alchemist.model.implementations.graph.PredefinedEnvGraphsKt;
import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData;
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph;
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon;
import kotlin.Pair;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Draws the navigation mesh of an environment, obtained using the Deaccon algorithm.
 */
public class DrawNavigationMesh implements Effect {

    /**
     */
    protected static final int MAX_COLOUR_VALUE = 255;
    /**
     */
    protected static final int INITIAL_ALPHA_DIVIDER = 5;
    /**
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
    @ExportForGUI(nameToExport = "number of seeds")
    private String nSeeds = "60";
    @ExportForGUI(nameToExport = "env start x (south-west)")
    private String envStartX = "0";
    @ExportForGUI(nameToExport = "env start y (south-west)")
    private String envStartY = "0";
    @ExportForGUI(nameToExport = "env end x (north-east)")
    private String envEndX = "300";
    @ExportForGUI(nameToExport = "env end y (north-east)")
    private String envEndY = "300";
    @ExportForGUI(nameToExport = "to be drawn")
    private boolean toBeDrawn;
    @ExportForGUI(nameToExport = "to be obtained")
    private boolean toBeObtained;
    @ExportForGUI(nameToExport = "draw underlying graph")
    private boolean drawGraph;
    private Color colorCache = Color.BLUE;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient NavigationGraph<Euclidean2DPosition, ?, ConvexPolygon, GraphEdgeWithData<ConvexPolygon, Pair<Euclidean2DPosition, Euclidean2DPosition>>> envGraph;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Optional<Node> markerNode = Optional.empty();

    /**
     * @param g        graphics
     * @param n        node
     * @param env      environment
     * @param wormhole the wormhole used to map environment's coords to screen coords
     * @param <T> concentration type
     * @param <P> position type
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
        if (markerNode.get() == n && envGraph != null) { // at this point markerNode.isPresent() is always true, so we directly get it
            final IWormhole2D<Euclidean2DPosition> w = (IWormhole2D<Euclidean2DPosition>) wormhole;
            colorCache = new Color(red.getVal(), green.getVal(), blue.getVal(), alpha.getVal());
            envGraph.nodes().stream()
                    .map(r -> mapEnvConvexPolygonToAwtShape(r, w))
                    .forEach(r -> {
                        g.setColor(colorCache);
                        g.fill(r);
                        g.setColor(colorCache.brighter().brighter());
                        g.draw(r);
                    });
            if (drawGraph) {
                envGraph.nodes().forEach(r -> {
                    final Point centroidFrom = w.getViewPoint(r.getCentroid());
                    envGraph.edgesFrom(r).forEach(e -> {
                        final Pair<Euclidean2DPosition, Euclidean2DPosition> c = e.getData();
                        final Point viewP1 = w.getViewPoint(c.getFirst());
                        final Point viewP2 = w.getViewPoint(c.getSecond());
                        g.setColor(Color.GREEN);
                        g.drawLine(viewP1.x, viewP1.y, viewP2.x, viewP2.y);
                        final Point midPoint = new Point((viewP1.x + viewP2.x) / 2, (viewP1.y + viewP2.y) / 2);
                        g.setColor(colorCache);
                        g.drawLine(centroidFrom.x, centroidFrom.y, midPoint.x, midPoint.y);
                    });
                });
            }
        }
        if ((toBeDrawn || toBeObtained) && !envStartX.equals("") && !envStartY.equals("") && !envEndX.equals("") && !envEndY.equals("")
                && env instanceof Environment2DWithObstacles
                && env.makePosition(0.0, 0.0) instanceof Euclidean2DPosition) {
            if (toBeDrawn) {
                final Double startX = Double.parseDouble(envStartX);
                final Double startY = Double.parseDouble(envStartY);
                final Double endX = Double.parseDouble(envEndX);
                final Double endY = Double.parseDouble(envEndY);
                envGraph = new Deaccon2D(Integer.parseInt(nSeeds)).generateEnvGraph(
                        new Point2D.Double(startX, startY),
                        Math.abs(endX - startX),
                        Math.abs(endY - startY),
                        ((Environment2DWithObstacles<?, T, Euclidean2DPosition>) env).getObstacles(),
                        new ArrayList());
                toBeDrawn = false;
            } else {
                envGraph = (NavigationGraph<Euclidean2DPosition, ?, ConvexPolygon, GraphEdgeWithData<ConvexPolygon, Pair<Euclidean2DPosition, Euclidean2DPosition>>>) PredefinedEnvGraphsKt.orientingSimulationEnvGraph(((Environment2DWithObstacles) env).getObstacles());
                toBeObtained = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColorSummary() {
        return colorCache;
    }

    private Shape mapEnvConvexPolygonToAwtShape(final ConvexPolygon p, final IWormhole2D<Euclidean2DPosition> wormhole) {
        final Path2D shape = new Path2D.Double();
        for (int i = 0; i < p.vertices().size(); i++) {
            final Point viewPoint = wormhole.getViewPoint(p.vertices().get(i));
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

    /**
     * @return the number of seeds
     */
    public String getnSeeds() {
        return nSeeds;
    }

    /**
     * @param nSeeds the number of seeds
     */
    public void setnSeeds(final String nSeeds) {
        this.nSeeds = nSeeds;
    }

    /**
     * @return env start x
     */
    public String getEnvStartX() {
        return envStartX;
    }

    /**
     * @param envStartX env start x
     */
    public void setEnvStartX(final String envStartX) {
        this.envStartX = envStartX;
    }

    /**
     * @return env start y
     */
    public String getEnvStartY() {
        return envStartY;
    }

    /**
     * @param envStartY env start y
     */
    public void setEnvStartY(final String envStartY) {
        this.envStartY = envStartY;
    }

    /**
     * @return env end x
     */
    public String getEnvEndX() {
        return envEndX;
    }

    /**
     * @param envEndX env end x
     */
    public void setEnvEndX(final String envEndX) {
        this.envEndX = envEndX;
    }

    /**
     * @return env end y
     */
    public String getEnvEndY() {
        return envEndY;
    }

    /**
     * @param envEndY env end y
     */
    public void setEnvEndY(final String envEndY) {
        this.envEndY = envEndY;
    }

    /**
     * @return if the underlying graph should be drawn
     */
    public boolean isDrawGraph() {
        return drawGraph;
    }

    /**
     * @param drawGraph if the underlying graph should be drawn
     */
    public void setDrawGraph(final boolean drawGraph) {
        this.drawGraph = drawGraph;
    }
}
