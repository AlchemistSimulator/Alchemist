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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;

import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Dimension2D;

/**
 * Draw layers concentration as background in the gui. The higher is the layer
 * concentration in a point, the higher is the alpha channel for the background
 * in that point.
 *
 * The user can specify:
 * - the number of samples for each side, basically more samples correspond to a smoother
 * and more detailed background
 * - the min concentration value
 * - the max concentration value
 *
 * The purpose and structure of this class is similar to {@link DrawLayersIsolines}.
 */
public abstract class DrawLayersConcentration extends DrawLayers {

    private static final long serialVersionUID = 1L;
    private static final int MIN_SAMPLES = 10;
    private static final int MAX_SAMPLES = 400;

    @ExportForGUI(nameToExport = "Samples for each side")
    private RangedInteger samples = new RangedInteger(MIN_SAMPLES, MAX_SAMPLES, MIN_SAMPLES * 10);
    @ExportForGUI(nameToExport = "Min concentration value")
    private String minConcentrationValue = "0.0";
    @ExportForGUI(nameToExport = "Max concentration value")
    private String maxConcentrationValue = "0.0";

    private String minConcentrationValueCached = minConcentrationValue;
    private String maxConcentrationValueCached = maxConcentrationValue;
    private Double minConcentrationValueDouble = Double.parseDouble(minConcentrationValue);
    private Double maxConcentrationValueDouble = Double.parseDouble(maxConcentrationValue);

    /**
     * Effectively draw the concentration of a bidimensional function.
     *
     * @param f        - the function
     * @param env      - the environment (it is used to make positions)
     * @param g        - the Graphics2D
     * @param wormhole - the wormhole
     * @param <T>      - concentration type
     * @param <P>      - position type
     */
    protected <T, P extends Position2D<P>> void drawConcentration(final IsolinesFinder.BidimensionalFunction f, final Environment<T, P> env, final Graphics2D g, final IWormhole2D<P> wormhole) {
        if (!minConcentrationValueCached.equals(minConcentrationValue) || !maxConcentrationValueCached.equals(maxConcentrationValue)) {
            updateMinAndMaxConcentrationValues();
        }

        final Dimension2D viewSize = wormhole.getViewSize();

        final int viewStartX = 0;
        final int viewStartY = 0;
        final int viewEndX = (int) Math.ceil(viewSize.getWidth());
        final int viewEndY = (int) Math.ceil(viewSize.getHeight());

        final int stepX = (viewEndX - viewStartX) / samples.getVal();
        final int stepY = (viewEndY - viewStartY) / samples.getVal();

        for (int i1 = viewStartX; i1 < viewEndX; i1 += stepX) {
            int i2 = i1 + stepX;
            for (int j1 = viewStartY; j1 < viewEndY; j1 += stepY) {
                int j2 = j1 + stepY;

                final Point p1 = new Point(i1, j1);
                final Point p2 = new Point(i1, j2);
                final Point p3 = new Point(i2, j1);
                final Point p4 = new Point(i2, j2);

                final P envP1 = wormhole.getEnvPoint(p1);
                final P envP2 = wormhole.getEnvPoint(p2);
                final P envP3 = wormhole.getEnvPoint(p3);
                final P envP4 = wormhole.getEnvPoint(p4);

                final double v1 = f.apply(envP1.getX(), envP1.getY()).doubleValue();
                final double v2 = f.apply(envP2.getX(), envP2.getY()).doubleValue();
                final double v3 = f.apply(envP3.getX(), envP3.getY()).doubleValue();
                final double v4 = f.apply(envP4.getX(), envP4.getY()).doubleValue();

                final double v = (v1 + v2 + v3 + v4) / 4;

                final double newAlpha = map(v, minConcentrationValueDouble, maxConcentrationValueDouble, 0, getAlpha().getVal());
                g.setColor(new Color(getRed().getVal(), getGreen().getVal(), getBlue().getVal(), (int) Math.ceil(newAlpha)));
                g.fillRect(i1, j1, i2 - i1, j2 - j1);
            }
        }
    }

    private void updateMinAndMaxConcentrationValues() {
        minConcentrationValueCached = minConcentrationValue;
        maxConcentrationValueCached = maxConcentrationValue;

        try {
            minConcentrationValueDouble = Double.parseDouble(minConcentrationValue);
            maxConcentrationValueDouble = Double.parseDouble(maxConcentrationValue);
        } catch (NumberFormatException e) {
            L.warn(minConcentrationValue + " or " + maxConcentrationValue + " is not a valid value");
        }
    }

    /**
     * @return the number of samples
     */
    public RangedInteger getSamples() {
        return samples;
    }

    /**
     * @param samples to set
     */
    public void setSamples(final RangedInteger samples) {
        this.samples = samples;
    }

    /**
     * @return a string representation of the min concentration value
     */
    public String getMinConcentrationValueString() {
        return minConcentrationValue;
    }

    /**
     * @param minConcentrationValue to set
     */
    public void setMinConcentrationValueString(final String minConcentrationValue) {
        this.minConcentrationValue = minConcentrationValue;
    }

    /**
     * @return a string representation of the max concentration value
     */
    public String getMaxConcentrationValueString() {
        return maxConcentrationValue;
    }

    /**
     * @param maxConcentrationValue to set
     */
    public void setMaxConcentrationValueString(final String maxConcentrationValue) {
        this.maxConcentrationValue = maxConcentrationValue;
    }

    /**
     * @return the min concentration value
     */
    public Double getMinConcentrationValue() {
        updateMinAndMaxConcentrationValues();
        return minConcentrationValueDouble;
    }

    /**
     * @param minConcentrationValue to set
     */
    public void setMinConcentrationValue(final Double minConcentrationValue) {
        this.minConcentrationValue = minConcentrationValue.toString();
    }

    /**
     * @return the max concentration value
     */
    public Double getMaxConcentrationValue() {
        updateMinAndMaxConcentrationValues();
        return maxConcentrationValueDouble;
    }

    /**
     * @param maxConcentrationValue to set
     */
    public void setMaxConcentrationValue(final Double maxConcentrationValue) {
        this.maxConcentrationValue = maxConcentrationValue.toString();
    }

    /*
     * maps x from [xmin, xmax] to [ymin, ymax]
     */
    private double map(final double x, final double xmin, final double xmax, final double ymin, final double ymax) {
        return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
    }
}
