/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors.utils;

/**
 * 
 */
public enum SubNaN {
    /**
     * 
     */
    ELIMINATE(Double.NaN), ZERO(0), ONE(1), POSITIVE_INFINITY(Double.POSITIVE_INFINITY), NEGATIVE_INFINITY(Double.NEGATIVE_INFINITY);

    private final double val;

    /**
     * @param substitution
     *            the value that should be substituted to NaN
     */
    SubNaN(final double substitution) {
        val = substitution;
    }

    /**
     * @return the value to use to replace {@link Double}.NaN
     */
    public double getSubstitutionValue() {
        return val;
    }
}
