/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export.filters;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.DoubleStream;

import it.unibo.alchemist.loader.export.FilteringPolicy;

/**
 * Utilities with the most common filtering operations on values.
 */
public enum CommonFilters {

    /**
     * Remove all {@link Double#NaN} values.
     */
    FILTERNAN((FilteringPolicy & java.io.Serializable) d -> Double.isNaN(d) ? DoubleStream.empty() : DoubleStream.of(d)),
    /**
     * Remove all values that match {@link Double#isInfinite(double)}  ({@link Double#NaN} don't get filtered).
     */
    FILTERINFINITY((FilteringPolicy & java.io.Serializable) d -> Double.isInfinite(d) ? DoubleStream.empty() : DoubleStream.of(d)),
    /**
     * Keeps only finite values ({@link Double#isFinite(double)} returns true).
     */
    ONLYFINITE((FilteringPolicy & java.io.Serializable) d -> Double.isFinite(d) ? DoubleStream.of(d) : DoubleStream.empty()),
    /**
     * Keeps all values.
     */
    NOFILTER((FilteringPolicy & java.io.Serializable) DoubleStream::of);

    private final FilteringPolicy filter;

    CommonFilters(final FilteringPolicy filter) {
        this.filter = filter;
    }

    /**
     * @return the {@link FilteringPolicy}
     */
    public FilteringPolicy getFilteringPolicy() {
        return filter;
    }

    /**
     * @param input a {@link String} matching a {@link FilteringPolicy} name (one of the values of {@link CommonFilters})
     * @return the corresponding {@link FilteringPolicy}
     */
    public static FilteringPolicy fromString(final String input) {
        return valueOf(Objects.requireNonNull(input).toUpperCase(Locale.ENGLISH)).getFilteringPolicy();
    }

}
