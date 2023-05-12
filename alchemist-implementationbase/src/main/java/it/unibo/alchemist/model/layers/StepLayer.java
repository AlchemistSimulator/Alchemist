/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.layers;

import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Position2D;

/**
 * Implements a {@link Layer} with a discontinue spatial distribution: the plane is divided 
 * in two parts, both with a constant concentration but with a different in value.
 * @param <T> the type describing the concentration in this {@link Layer}.
 * @param <P> {@link Position2D} type.
 *
 */
public final class StepLayer<T, P extends Position2D<? extends P>> implements Layer<T, P> {

    /**
     * 
     */
    private static final long serialVersionUID = -4002670240161927416L;
    private final double maxx;
    private final double maxy;
    private final T highValue;
    private final T lowValue;

    /**
     * Initialize a {@link StepLayer}.
     * @param mx the x value above which the concentration in layer is at its maximum value
     * @param my the y value above which the concentration in layer is at its maximum value
     * @param minValue the low value of concentration.
     * @param maxValue the high value of concentration.
     */
    public StepLayer(final double mx, final double my, final T maxValue, final T minValue) {
        maxx = mx;
        maxy = my;
        highValue = maxValue;
        lowValue = minValue;
    }

    /**
     * Initialize a {@link StepLayer} where concentration is at its maximum value
     * in first quadrant (for positive values of x and y).
     * @param maxValue minValue the low value of concentration.
     * @param minValue maxValue the high value of concentration.
     */
    public StepLayer(final T maxValue, final T minValue) {
        this(0, 0, maxValue, minValue);
    }

    @Override
    public T getValue(final P p) {
        if (p.getX() > maxx && p.getY() > maxy) {
            return highValue;
        } else {
            return lowValue;
        }
    }

}
