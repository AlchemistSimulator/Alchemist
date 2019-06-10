/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * 
 * Interface for time representation.
 */
public interface Time extends Comparable<Time>, Serializable {

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
