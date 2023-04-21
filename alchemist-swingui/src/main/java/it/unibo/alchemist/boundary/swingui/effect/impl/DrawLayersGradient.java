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
import it.unibo.alchemist.boundary.swingui.effect.api.LayerToFunctionMapper;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;
import org.danilopianini.lang.RangedInteger;
import org.danilopianini.view.ExportForGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.util.function.Function;

/**
 * Draw layers values as background in the gui. The higher is the layer
 * concentration in a point, the higher is the alpha channel for the background
 * in that point.
 *
 * The user must specify:
 * - the number of samples for each side, basically more samples correspond to a smoother
 * and more detailed background
 * - the min layer value
 * - the max layer value
 *
 * The purpose and structure of this class is similar to {@link DrawLayersIsolines}.
 */
public abstract class DrawLayersGradient extends DrawLayersValues {

    private static final long serialVersionUID = 1L;
    private static final int MIN_SAMPLES = 10;
    private static final int MAX_SAMPLES = 400;
    @ExportForGUI(nameToExport = "Samples for each side")
    private RangedInteger samples = new RangedInteger(MIN_SAMPLES, MAX_SAMPLES, MIN_SAMPLES * 10);

    /**
     * @param mapper the {@link LayerToFunctionMapper} converting a layer into a function.
     */
    protected DrawLayersGradient(final LayerToFunctionMapper mapper) {
        super(mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, P extends Position2D<P>> void drawFunction(
            final Function<? super P, ? extends Number> function,
            final Environment<T, P> environment,
            final Graphics2D graphics,
            final Wormhole2D<P> wormhole
    ) {
        if (minOrMaxLayerValuesNeedsToBeUpdated()) {
            updateMinAndMaxLayerValues();
        }
        // to draw the gradient, we simply divide the screen into cells, then we
        // visit each cell and determine its value by interpolating the values
        // at the corners. We then map such value to a color and fill the cell
        // with that color
        final Dimension2D viewSize = wormhole.getViewSize();
        final int viewStartX = 0;
        final int viewStartY = 0;
        final int viewEndX = (int) Math.ceil(viewSize.getWidth());
        final int viewEndY = (int) Math.ceil(viewSize.getHeight());
        // step is the side of each cell
        final int stepX = (viewEndX - viewStartX) / samples.getVal();
        final int stepY = (viewEndY - viewStartY) / samples.getVal();
        // visiting a cell means having the indexes of its four corners
        for (int i1 = viewStartX; i1 < viewEndX; i1 += stepX) {
            final int i2 = i1 + stepX;
            for (int j1 = viewStartY; j1 < viewEndY; j1 += stepY) {
                final int j2 = j1 + stepY;
                // the four points of the view
                final Point p1 = new Point(i1, j1);
                final Point p2 = new Point(i1, j2);
                final Point p3 = new Point(i2, j1);
                final Point p4 = new Point(i2, j2);
                // we map them to env points
                final P envP1 = wormhole.getEnvPoint(p1);
                final P envP2 = wormhole.getEnvPoint(p2);
                final P envP3 = wormhole.getEnvPoint(p3);
                final P envP4 = wormhole.getEnvPoint(p4);
                // get the values
                final double v1 = function.apply(envP1).doubleValue();
                final double v2 = function.apply(envP2).doubleValue();
                final double v3 = function.apply(envP3).doubleValue();
                final double v4 = function.apply(envP4).doubleValue();
                // interpolate such values
                final double v = (v1 + v2 + v3 + v4) / 4;
                // fill the cell with the color
                if (v >= getMinLayerValueDouble()) {
                    final double newAlpha = map(v, getMinLayerValueDouble(), getMaxLayerValueDouble(), 0, getAlpha().getVal());
                    graphics.setColor(new Color(
                            getRed().getVal(),
                            getGreen().getVal(),
                            getBlue().getVal(),
                            (int) Math.ceil(newAlpha)
                    ));
                    graphics.fillRect(i1, j1, i2 - i1, j2 - j1);
                }
            }
        }
    }

    /**
     * @return the number of samples
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public RangedInteger getSamples() {
        return samples;
    }

    /**
     * @param samples to set
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public void setSamples(final RangedInteger samples) {
        this.samples = samples;
    }

    /**
     * Map x from [xmin, xmax] to [ymin, ymax].
     *
     * @param x    - x
     * @param xmin - the lower bound of the actual x scale (included)
     * @param xmax - the upper bound of the actual x scale (included)
     * @param ymin - the lower bound of the new x scale (included)
     * @param ymax - the upper bound of the new x scale (included)
     *
     * @return the new value for x
     */
    public static double map(final double x, final double xmin, final double xmax, final double ymin, final double ymax) {
        return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
    }
}
