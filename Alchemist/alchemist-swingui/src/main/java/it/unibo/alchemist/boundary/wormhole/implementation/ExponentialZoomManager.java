/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;

/**
 * An <code>ExpZoomManager</code> converts the sliding of any physical/virtual
 * device/control into a zoom rate through an exponential function (in this way
 * I am sure to not see negative values ;-).<br>
 * Zoom = base ^ (amount of slides / normalization value).
 * 

 */
public class ExponentialZoomManager extends AbstractSlideInputManager implements ZoomManager {
    /**
     * DEF_BASE = "DEFault BASE".<br>
     * It is the default base for the exponential function. It is meant to be
     * greater than 1. Currently its value is {@value #DEF_BASE} that seems
     * pretty good to me.
     */
    public static final double DEF_BASE = 1.1d;

    private double normal;
    private double base;

    /**
     * Calculates the initial amount of slides (i.e. <code>value</code>) to have
     * a zoom rate equal to <code>z</code>.
     * 
     * @param z
     *            is the desired zoom rate
     * @param b
     *            is the base
     * @param n
     *            is the normalization value
     * @return a <code>double</code> value representing the initial amount of
     *         slides
     */
    protected static double getSlideValueFromZoom(final double z, final double b, final double n) {
        return n * Math.log(z) / Math.log(b);
    }

    /**
     * Same of {@link #ExponentialZoomManager(double, double, double)} but normalization
     * value is 1.
     * 
     * @param b
     *            is the base of the exponential function
     * @param z
     *            is the desired initial zoom rate
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

    /**
     * Allow any child class to see the base of the exponential.
     * 
     * @return a <code>double</code> value representing the base of the
     *         exponential
     */
    protected double getBase() {
        return base;
    }

    /**
     * Allow any child class to see the normalization value.
     * 
     * @return a <code>double</code> value representing the normalization value
     */
    protected double getNormal() {
        return normal;
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

    /**
     * Allow any child class to modify the base of the exponential.
     * 
     * @param b
     *            is the <code>double</code> value representing the base of the
     *            exponential
     */
    protected void setBase(final double b) {
        base = b;
    }

    /**
     * Allow any child class to modify the normalization value.
     * 
     * @param n
     *            is the <code>double</code> value representing the
     *            normalization value
     */
    protected void setNormal(final double n) {
        normal = n;
    }

    @Override
    public void setZoom(final double rate) {
        setValue(getSlideValueFromZoom(rate, base, normal));
    }
}
