/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serializable;

/**
 * 
 * Interface for time representation.
 */
public interface Time extends Comparable<Time>, Serializable {

    /**
     * Initial time.
     */
    Time ZERO = new Time() {
        @Override
        public boolean isInfinite() {
            return false;
        }

        @Override
        public Time times(final double var) {
            if (Double.isFinite(var)) {
                return ZERO;
            }
            throw new IllegalArgumentException("Cannot multiply zero by " + var);
        }

        @Override
        public Time minus(final Time dt) {
            return dt.times(-1);
        }

        @Override
        public Time plus(final Time dt) {
            return dt;
        }

        @Override
        public double toDouble() {
            return 0;
        }

        @Override
        public int compareTo(@NonNull final Time o) {
            return Double.compare(0, o.toDouble());
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof Time && ((Time) obj).toDouble() == 0;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(0);
        }

        @Override
        public String toString() {
            return "0";
        }
    };

    /**
     * Indefinitely future time.
     */
    Time INFINITY = new Time() {
        @Override
        public boolean isInfinite() {
            return true;
        }

        @Override
        public Time times(final double var) {
            if (var > 0) {
                return INFINITY;
            }
            throw new IllegalArgumentException("Cannot multiply an infinite time by " + var);
        }

        @Override
        public Time minus(final Time dt) {
            return INFINITY;
        }

        @Override
        public Time plus(final Time dt) {
            return INFINITY;
        }

        @Override
        public double toDouble() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public int compareTo(@NonNull final Time o) {
            return Double.compare(Double.POSITIVE_INFINITY, o.toDouble());
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this
                || obj instanceof Time && ((Time) obj).toDouble() == Double.POSITIVE_INFINITY;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(Double.POSITIVE_INFINITY);
        }

        @Override
        public String toString() {
            return "∞";
        }
    };

    /**
     * Verifies if the {@link Time} is set at infinite, namely if the event will
     * never happen.
     * 
     * @return true if the {@link Time} is infinite
     */
    boolean isInfinite();

    /**
     * Allows to multiply this {@link Time} for a constant.
     * 
     * @param var
     *            the {@link Time} to sum to the current {@link Time}
     * 
     * @return the result of the multiplication
     * 
     */
    Time times(double var);

    /**
     * Allows to subtract a {@link Time} to this {@link Time}.
     * 
     * @param dt
     *            the time to subtract from the current {@link Time}
     * 
     * @return the result of the subtraction
     */
    Time minus(Time dt);

    /**
     * Allows to add a {@link Time} to this {@link Time}.
     * 
     * @param dt
     *            the time to sum to the current {@link Time}
     * 
     * @return the result of the sum
     */
    Time plus(Time dt);

    /**
     * Allows to get a double representation of this {@link Time}.
     * 
     * @return the double representation of this {@link Time}
     */
    double toDouble();

}
