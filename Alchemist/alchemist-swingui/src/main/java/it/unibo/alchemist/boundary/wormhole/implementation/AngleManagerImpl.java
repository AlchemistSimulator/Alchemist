/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.AngleManager;

import java.awt.Toolkit;

/**
 * An <code>AngleManager</code> converts the sliding of any physical/virtual
 * device/control into an angle expressed with radians.
 * 

 */
public class AngleManagerImpl extends AbstractSlideInputManager implements AngleManager {
    /**
     * <code>DEF_DEG_PER_WHEEL_CLICK</code> =
     * "DEFault amount of DEGrees PER WHEEL CLICK".<br>
     * Currently set to {@value #DEF_DEG_PER_WHEEL_CLICK} that seems pretty good
     * to me.
     */
    public static final double DEF_DEG_PER_WHEEL_CLICK = 5d;

    /**
     * <code>DEF_DEG_PER_PIXEL</code> = "DEFault amount of DEGrees PER PIXEL".<br>
     * Currently set to <code>3 * 360 / screenWidth</code> i.e.: by sliding the
     * mouse through the entire width of the screen you will obtain a 3 rounds
     * rotation.<br>
     * This constant is intended for an implementation that converts mouse
     * motion over x axis into degrees.
     */
    public static final double DEF_DEG_PER_PIXEL = 3d * 360 / Toolkit.getDefaultToolkit().getScreenSize().getWidth();

    private double degPhase;
    private double degUnit;

    /**
     * Initializes a new <code>AngleManager</code> instance with
     * <code>dUnit = 1</code> and <code>dPhase = 0</code>.
     * 
     * @see #AngleManagerImpl(double, double)
     */
    public AngleManagerImpl() {
        this(1d);
    }

    /**
     * Initializes a new <code>AngleManager</code> instance with
     * <code>dPhase = 0</code>.
     * 
     * @param dUnit
     *            is the amount of degrees corresponding to 1 slide (i.e.
     *            <code>inc(1);</code>)
     * @see #AngleManagerImpl(double, double)
     */
    public AngleManagerImpl(final double dUnit) {
        this(dUnit, 0d);
    }

    /**
     * Initializes a new <code>AngleManager</code> with the initial phase and
     * unit in input.
     * 
     * @param dUnit
     *            is the amount of degrees corresponding to 1 slide (i.e.
     *            <code>inc(1);</code>)
     * @param dPhase
     *            is the initial rotation expressed with degrees
     */
    public AngleManagerImpl(final double dUnit, final double dPhase) {
        super(0, Double.MIN_VALUE, Double.MAX_VALUE);
        degUnit = dUnit;
        degPhase = dPhase;
    }

    @Override
    public double getAngle() {
        return Math.toRadians(getValue() * degUnit + degPhase);
    }

    /**
     * Allow any child class to see the initial phase.
     * 
     * @return a <code>double</code> value representing the initial phase
     */
    protected double getDegPhase() {
        return degPhase;
    }

    /**
     * Allow any child class to see the degrees-per-slide unit.
     * 
     * @return a <code>double</code> value representing the degrees-per-slide
     *         unit
     */
    protected double getDegUnit() {
        return degUnit;
    }

    /**
     * Allow any child class to modify the initial phase.
     * 
     * @param dPhase
     *            is the <code>double</code> value representing the initial
     *            phase
     */
    protected void setDegPhase(final double dPhase) {
        degPhase = dPhase;
    }

    /**
     * Allow any child class to modify the degrees-per-slide unit.
     * 
     * @param dUnit
     *            is the <code>double</code> value representing the
     *            degrees-per-slide unit
     */
    protected void setDegUnit(final double dUnit) {
        degUnit = dUnit;
    }

}
