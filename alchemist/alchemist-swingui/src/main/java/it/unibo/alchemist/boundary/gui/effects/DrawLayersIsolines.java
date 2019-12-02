/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.isolines.IsolinesFinder;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Environment;

import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Draw layers isolines. The user must specify:
 * - the number of isolines to draw
 * - the min layer value
 * - the max layer value
 * - the distribution, used to space isoline values between min and max
 *
 * This class defines the {@link DrawLayersIsolines#drawFunction(Function, Environment, Graphics2D, IWormhole2D)}
 * method, which is capable of drawing a layer's isolines given a function.
 * The only responsibility left to subclasses is to provide a {@link LayerToFunctionMapper}.
 */
public abstract class DrawLayersIsolines extends DrawLayersValues {

    private static final long serialVersionUID = 1L;
    private static final int MAX_NUMBER_OF_ISOLINES = 50;
    @ExportForGUI(nameToExport = "Number of isolines")
    private RangedInteger nOfIsolines = new RangedInteger(1, MAX_NUMBER_OF_ISOLINES, MAX_NUMBER_OF_ISOLINES / 4);
    @ExportForGUI(nameToExport = "Distribution between min and max")
    private Distribution distribution = Distribution.LINEAR;

    private int nOfIsolinesCached = nOfIsolines.getVal();
    private Distribution distributionCached = distribution;
    private Collection<Number> levels;

    /**
     * The algorithm used to extract isolines.
     */
    private final IsolinesFinder algorithm;

    /**
     * Every class extending this one should call this constructor.
     *
     * @param algorithm - the algorithm used to extract isolines
     */
    public DrawLayersIsolines(final IsolinesFinder algorithm) {
        super();
        Objects.requireNonNull(algorithm);
        this.algorithm = algorithm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, P extends Position2D<P>> void drawFunction(final Function<? super P, ? extends Number> f, final Environment<T, P> env, final Graphics2D g, final IWormhole2D<P> wormhole) {
        final Dimension2D viewSize = wormhole.getViewSize();
        final int viewStartX = 0;
        final int viewStartY = 0;
        final int viewEndX = (int) Math.ceil(viewSize.getWidth());
        final int viewEndY = (int) Math.ceil(viewSize.getHeight());
        final Point viewStart = new Point(viewStartX, viewStartY);
        final Point viewEnd = new Point(viewEndX, viewEndY);
        final P envStart = wormhole.getEnvPoint(viewStart);
        final P envEnd = wormhole.getEnvPoint(viewEnd);
        if (nOfIsolinesCached != nOfIsolines.getVal() || minOrMaxLayerValuesNeedsToBeUpdated() || distributionCached != distribution) {
            nOfIsolinesCached = nOfIsolines.getVal();
            updateMinAndMaxLayerValues();
            distributionCached = distribution;

            levels = Arrays.stream(
                    distribution == Distribution.LOGARITHMIC
                            ? logspace(getMinLayerValueDouble(), getMaxLayerValueDouble(), nOfIsolines.getVal(), Math.E)
                            : linspace(getMinLayerValueDouble(), getMaxLayerValueDouble(), nOfIsolines.getVal())
            ).boxed().collect(Collectors.toList());
        }
        algorithm.findIsolines((x, y) -> f.apply(env.makePosition(x, y)),
                envStart.getX(), envStart.getY(), envEnd.getX(), envEnd.getY(), levels).forEach(isoline -> {
            // draw isoline value
            isoline.getSegments().stream().findAny().ifPresent(segment -> {
                final Point viewPoint = wormhole.getViewPoint(env.makePosition(segment.getX1(), segment.getY1()));
                final int x = (int) Math.ceil(viewPoint.getX());
                final int y = (int) Math.ceil(viewPoint.getY());
                g.drawString(isoline.getValue().toString(), x, y);
            });
            // draw isoline
            isoline.getSegments().forEach(segment -> {
                final Point start = wormhole.getViewPoint(env.makePosition(segment.getX1(), segment.getY1()));
                final Point end = wormhole.getViewPoint(env.makePosition(segment.getX2(), segment.getY2()));
                final int x1 = (int) Math.ceil(start.getX());
                final int y1 = (int) Math.ceil(start.getY());
                final int x2 = (int) Math.ceil(end.getX());
                final int y2 = (int) Math.ceil(end.getY());
                g.drawLine(x1, y1, x2, y2);
            });
        });
    }

    /**
     * @return the number of isolines
     */
    public RangedInteger getNumberOfIsolines() {
        return nOfIsolines;
    }

    /**
     * @param nOfIsolines the number of isolines
     */
    public void setNumberOfIsolines(final RangedInteger nOfIsolines) {
        this.nOfIsolines = nOfIsolines;
    }

    /**
     * @return the current distribution
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     * @param distribution to set
     */
    public void setDistribution(final Distribution distribution) {
        this.distribution = distribution;
    }

    /**
     * @return the algorithm used to extract isolines
     */
    protected IsolinesFinder getIsolinesFinder() {
        return algorithm;
    }

    /**
     * Distributions describing how values within an interval should be spaced.
     */
    public enum Distribution {
        /**
         */
        LINEAR, LOGARITHMIC;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * generates n logarithmically-spaced points between d1 and d2 using the
     * provided base.
     *
     * @param d1 The min value
     * @param d2 The max value
     * @param n The number of points to generated
     * @param base the logarithmic base to use
     * @return an array of lineraly space points.
     */
    public static double[] logspace(final double d1, final double d2, final int n, final double base) {
        final double[] y = new double[n];
        final double[] p = linspace(d1, d2, n);
        for (int i = 0; i < y.length - 1; i++) {
            y[i] = Math.pow(base, p[i]);
        }
        y[y.length - 1] = Math.pow(base, d2);
        return y;
    }

    /**
     * generates n linearly-spaced points between d1 and d2.
     *
     * @param d1 The min value
     * @param d2 The max value
     * @param n The number of points to generated
     * @return an array of lineraly space points.
     */
    public static double[] linspace(final double d1, final double d2, final int n) {
        final double[] y = new double[n];
        final double dy = (d2 - d1) / (n - 1);
        for (int i = 0; i < n; i++) {
            y[i] = d1 + (dy * i);
        }
        return y;
    }
}

