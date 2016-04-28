/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.ISlideInputManager;

/**
 * <code>ASlideInputManager</code> is the base class for any class whose aim is
 * to handle the the sliding of any physical/virtual device/control.
 * 
 * 
 */
public class AbstractSlideInputManager implements ISlideInputManager {

    private double value;
    private final double min, max;

    /**
     * Creates a new <code>ASlideInputManager</code> with the value in input.
     * <br>
     * This is useful if you want to have an initial output.
     * 
     * @param initialValue
     *            is the hypothetical initial amound of 'slides'
     * @param min
     *            the minimum allowed
     * @param max
     *            the maximum allowed
     */
    public AbstractSlideInputManager(final double initialValue, final double min, final double max) {
        value = initialValue;
        this.min = min;
        this.max = max;
    }

    @Override
    public void dec(final double val) {
        setValue(value - val);
    }

    /**
     * Allow any child class to see the current value.
     * 
     * @return the current amount of 'slides'
     */
    protected double getValue() {
        return value;
    }

    @Override
    public void inc(final double val) {
        setValue(value + val);
    }

    /**
     * Allow any child class to modify the value directly.
     * 
     * @param val
     *            is the new value
     */
    protected void setValue(final double val) {
        value = val < min ? min : val > max ? max : val;
    }

}
