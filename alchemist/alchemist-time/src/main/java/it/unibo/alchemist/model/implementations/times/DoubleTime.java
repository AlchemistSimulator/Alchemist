/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.times;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Time;

/**
 *         This class is meant to provide a reasonably fast time implementation.
 *         Should be suitable for most usages, but it inherits the problem of
 *         the loss of precision of double numbers when comparing big numbers
 *         with low numbers. It could become a real problem with long
 *         simulations.
 * 
 */
public final class DoubleTime implements Time {

    private static final long serialVersionUID = -6332407580176508417L;
    /**
     * Infinite time.
     */
    public static final DoubleTime INFINITE_TIME = new DoubleTime(Double.POSITIVE_INFINITY);
    /**
     * Time zero.
     */
    public static final DoubleTime ZERO_TIME = new DoubleTime(0d);
    /**
     * 
     */
    private final double t;

    /**
     * Default empty constructor, builds a DoubleTime with value 0.
     */
    public DoubleTime() {
        t = 0;
    }

    /**
     * Builds a new DoubleTime starting from the specified value.
     * 
     * @param val
     *            the starting value of the time.
     */
    public DoubleTime(final double val) {
        t = val;
    }

    @Override
    public DoubleTime sum(final Time dt) {
        return new DoubleTime(t + dt.toDouble());
    }

    @Override
    public double toDouble() {
        return t;
    }

    @Override
    public int compareTo(final Time o) {
        final boolean inf = isInfinite();
        final boolean oinf = o.isInfinite();
        if (inf && oinf) {
            return 0;
        } else if (inf) {
            return 1;
        } else if (oinf) {
            return -1;
        }
        final double od = o.toDouble();
        return t > od ? 1 : t < od ? -1 : 0;
    }

    @Override
    public boolean isInfinite() {
        return t == Double.POSITIVE_INFINITY;
    }

    @Override
    public Time multiply(final double var) {
        return new DoubleTime(t * var);
    }

    @Override
    public Time subtract(final Time dt) {
        return new DoubleTime(t - dt.toDouble());
    }

    @Override
    @SuppressFBWarnings(justification = "Stateless object.")
    public DoubleTime clone() {
        return this;
    }

    @Override
    @SuppressFBWarnings(justification = "I need exact comparison here")
    public boolean equals(final Object obj) {
        return obj instanceof DoubleTime && ((DoubleTime) obj).toDouble() == t;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(t).hashCode();
    }

    @Override
    public String toString() {
        return Double.toString(t);
    }

}
