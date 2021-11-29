/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export.filters;

import it.unibo.alchemist.loader.export.FilteringPolicy;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * Utilities with the most common filtering operations on values.
 */
public enum CommonFilters {

    /**
    /**
     * Remove all {@link Double#NaN} values.
     */
    FILTERNAN((FilteringPolicy & java.io.Serializable) d -> Double.isNaN(d) ? emptyList() : List.of(d)),
    /**
     * Remove all values that match {@link Double#isInfinite(double)}  ({@link Double#NaN} don't get filtered).
     */
    FILTERINFINITY((FilteringPolicy & java.io.Serializable) d -> Double.isInfinite(d)
            ? emptyList()
            : List.of(d)),
    /**
     * Keeps only finite values ({@link Double#isFinite(double)} returns true).
     */
    ONLYFINITE((FilteringPolicy & java.io.Serializable) d -> Double.isFinite(d) ? List.of(d) : emptyList()),
    /**
     * Keeps all values.
     */
    NOFILTER((FilteringPolicy & java.io.Serializable) List::of);

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
