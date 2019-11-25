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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO().
 */
public abstract class DrawLayersIsolines extends DrawLayers {

    private static final int MAX_NUMBER_OF_ISOLINES = 50;
    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "Number of isolines")
    private RangedInteger nOfIsolines = new RangedInteger(1, MAX_NUMBER_OF_ISOLINES, MAX_NUMBER_OF_ISOLINES / 4);
    @ExportForGUI(nameToExport = "Min isoline value")
    private String minIsolineValue = "0.0";
    @ExportForGUI(nameToExport = "Max isoline value")
    private String maxIsolineValue = "0.0";
    @ExportForGUI(nameToExport = "Distribution between min and max")
    private Distribution distribution = Distribution.LINEAR;

    private int nOfIsolinesCached = nOfIsolines.getVal();
    private String minIsolineValueCached = minIsolineValue;
    private String maxIsolineValueCached = maxIsolineValue;
    private Distribution distributionCached = distribution;
    private double[] levels;
    private Double minIsolineValueDouble = Double.parseDouble(minIsolineValue);
    private Double maxIsolineValueDouble = Double.parseDouble(maxIsolineValue);

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
     * Effectively draw the isolines for the given bidimensional function.
     *
     * @param f        - the function
     * @param env      - the environment (it is used to make positions)
     * @param g        - the Graphics2D
     * @param wormhole - the wormhole
     * @param <T>      - concentration type
     * @param <P>      - position type
     */
    protected <T, P extends Position2D<P>> void drawIsolines(final IsolinesFinder.BidimensionalFunction f, final Environment<T, P> env, final Graphics2D g, final IWormhole2D<P> wormhole) {
        final Dimension2D viewSize = wormhole.getViewSize();

        final int viewStartX = 0;
        final int viewStartY = 0;
        final int viewEndX = (int) Math.ceil(viewSize.getWidth());
        final int viewEndY = (int) Math.ceil(viewSize.getHeight());

        final Point viewStart = new Point(viewStartX, viewStartY);
        final Point viewEnd = new Point(viewEndX, viewEndY);

        final P envStart = wormhole.getEnvPoint(viewStart);
        final P envEnd = wormhole.getEnvPoint(viewEnd);

        if (nOfIsolinesCached != nOfIsolines.getVal()
                || !minIsolineValueCached.equals(minIsolineValue)
                || !maxIsolineValueCached.equals(maxIsolineValue)
                || distributionCached != distribution) {
            nOfIsolinesCached = nOfIsolines.getVal();
            updateMinAndMaxLayerValues();
            distributionCached = distribution;

            if (distribution == Distribution.LOGARITHMIC) {
                levels = logspace(minIsolineValueDouble, maxIsolineValueDouble, nOfIsolines.getVal(), Math.E);
            } else {
                levels = linspace(minIsolineValueDouble, maxIsolineValueDouble, nOfIsolines.getVal());
            }
        }

        algorithm.findIsolines(f, envStart.getX(), envStart.getY(), envEnd.getX(), envEnd.getY(), Arrays.stream(levels).boxed().collect(Collectors.toList())).forEach(isoline -> {
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

    private void updateMinAndMaxLayerValues() {
        minIsolineValueCached = minIsolineValue;
        maxIsolineValueCached = maxIsolineValue;

        try {
            minIsolineValueDouble = Double.parseDouble(minIsolineValue);
            maxIsolineValueDouble = Double.parseDouble(maxIsolineValue);
        } catch (NumberFormatException e) {
            L.warn(minIsolineValue + " or " + maxIsolineValue + " is not a valid value");
        }
    }

    /**
     * @return the number of contour levels
     */
    public RangedInteger getNumberOfIsolines() {
        return nOfIsolines;
    }

    /**
     * @param contourLevels the number of contour levels
     */
    public void setNumberOfIsolines(final RangedInteger contourLevels) {
        this.nOfIsolines = contourLevels;
    }

    /**
     * @return a string representation of the min isoline value
     */
    public String getMinIsolineValueString() {
        return minIsolineValue;
    }

    /**
     * @return the min isoline value
     */
    public Double getMinIsolineValue() {
        updateMinAndMaxLayerValues();
        return minIsolineValueDouble;
    }

    /**
     * @param minIsolineValue to set
     */
    public void setMinIsolineValueString(final String minIsolineValue) {
        this.minIsolineValue = minIsolineValue;
    }

    /**
     * @param minIsolineValue to set
     */
    public void setMinIsolineValue(final Double minIsolineValue) {
        this.minIsolineValue = minIsolineValue.toString();
    }

    /**
     * @return a string representation of the maximum isoline value
     */
    public String getMaxIsolineValueString() {
        return maxIsolineValue;
    }

    /**
     * @return the max isoline value
     */
    public Double getMaxIsolineValue() {
        updateMinAndMaxLayerValues();
        return maxIsolineValueDouble;
    }

    /**
     * @param maxIsolineValue to set
     */
    public void setMaxIsolineValueString(final String maxIsolineValue) {
        this.maxIsolineValue = maxIsolineValue;
    }

    /**
     * @param maxIsolineValue to set
     */
    public void setMaxIsolineValue(final Double maxIsolineValue) {
        this.maxIsolineValue = maxIsolineValue.toString();
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
     * Distributions describing how values within the
     * [minLayerValue, maxLayerValue] interval will be spaced.
     */
    public enum Distribution {
        /**
         */
        LINEAR, LOGARITHMIC;

        @Override
        public String toString() {
            final String sup = super.toString();
            final StringBuilder sb = new StringBuilder(2 * sup.length());
            if (!sup.isEmpty()) {
                sb.append(sup.charAt(0));
            }
            for (int i = 1; i < sup.length(); i++) {
                final char curChar = sup.charAt(i);
                if (Character.isUpperCase(curChar)) {
                    sb.append(' ');
                }
                sb.append(curChar);
            }
            return sb.toString();
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

