/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.times;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Time;

import javax.annotation.Nonnull;

/**
 *         This class is meant to provide a reasonably fast time implementation.
 *         Should be suitable for most usages, but it inherits the problem of
 *         the loss of precision of double numbers when comparing big numbers
 *         with low numbers. It could become a real problem with long
 *         simulations.
 * 
 */
public final class DoubleTime implements Time {

    private static final long serialVersionUID = 1L;
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
    @Nonnull
    public DoubleTime plus(final Time dt) {
        return new DoubleTime(t + dt.toDouble());
    }

    @Override
    public double toDouble() {
        return t;
    }

    @Override
    public int compareTo(final Time o) {
        return Double.compare(toDouble(), o.toDouble());
    }

    @Override
    public boolean isInfinite() {
        return t == Double.POSITIVE_INFINITY;
    }

    @Override
    @Nonnull
    public Time times(final double var) {
        return new DoubleTime(t * var);
    }

    @Override
    @Nonnull
    public Time minus(final Time dt) {
        return new DoubleTime(t - dt.toDouble());
    }

    @Override
    @SuppressFBWarnings(justification = "I need exact comparison here")
    public boolean equals(final Object obj) {
        return obj instanceof DoubleTime && ((DoubleTime) obj).toDouble() == t;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(t);
    }

    @Override
    public String toString() {
        return Double.toString(t);
    }

}
