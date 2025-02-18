/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.ui.impl;

import it.unibo.alchemist.boundary.ui.api.ZoomManager;

/**
 * An <code>ExpZoomManager</code> converts the sliding of any physical/virtual
 * device/control into a zoom rate through an exponential function (in this way
 * I am sure to not see negative values ;-).<br>
 * Zoom = base ^ (number of slides / normalization value).
 *
 */
public final class ExponentialZoomManager extends BaseSlideInputManager implements ZoomManager {
    /**
     * DEF_BASE = "DEFault BASE".<br>
     * It is the default base for the exponential function. It is meant to be
     * greater than 1. Currently, its value is { @value #DEF_BASE } that seems
     * pretty good to me.
     */
    public static final double DEF_BASE = 1.1d;

    private final double normal;
    private final double base;

    /**
     * The same of {@link #ExponentialZoomManager(double, double, double)} but normalization
     * value is 1.
     *
     * @param b
     *            the base of the exponential function
     * @param z
     *            the desired initial zoom rate
     */
    public ExponentialZoomManager(final double z, final double b) {
        this(z, b, 1d);
    }

    /**
     * Initialize a new <code>ExpZoomManager</code> instance with the parameters
     * in input.
     *
     * @param z
     *            is the desired initial zoom rate
     * @param b
     *            is the base of the exponential function
     * @param n
     *            is the normalization value
     */
    public ExponentialZoomManager(final double z, final double b, final double n) {
        super(getSlideValueFromZoom(z, b, n), -Double.MAX_VALUE, Double.MAX_VALUE);
        normal = Math.abs(n);
        base = Math.abs(b);
    }

    @Override
    public double getZoom() {
        final double val = getValue();
        if (val == 0) {
            return 1d;
        } else if (val < 0) {
            return Math.pow(1 / base, -val / normal);
        } else {
            return Math.pow(base, val / normal);
        }
    }

    @Override
    public void setZoom(final double rate) {
        setValue(getSlideValueFromZoom(rate, base, normal));
    }

    /**
     * Calculates the initial number of slides (i.e. <code>value</code>) to have
     * a zoom rate equal to <code>z</code>.
     *
     * @param z
     *            the desired zoom rate
     * @param b
     *            the base
     * @param n
     *            the normalization value
     * @return a <code>double</code> value representing the initial number of
     *         slides
     */
    private static double getSlideValueFromZoom(final double z, final double b, final double n) {
        if (z <= 0 || b <= 0) {
            throw new IllegalArgumentException("Not zoom nor the base can be zero or negative. (zoom=" + z + ", base=" + b + ")");
        }
        return n * Math.log(z) / Math.log(b);
    }
}
