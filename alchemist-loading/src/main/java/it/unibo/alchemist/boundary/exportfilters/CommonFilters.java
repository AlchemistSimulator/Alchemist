/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.exportfilters;

import it.unibo.alchemist.boundary.ExportFilter;

import java.io.Serializable;
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
    FILTERNAN((ExportFilter & Serializable) d -> Double.isNaN(d) ? emptyList() : List.of(d)),
    /**
     * Remove all values that match {@link Double#isInfinite(double)}  ({@link Double#NaN} don't get filtered).
     */
    FILTERINFINITY((ExportFilter & Serializable) d -> Double.isInfinite(d)
            ? emptyList()
            : List.of(d)),
    /**
     * Keeps only finite values ({@link Double#isFinite(double)} returns true).
     */
    ONLYFINITE((ExportFilter & Serializable) d -> Double.isFinite(d) ? List.of(d) : emptyList()),
    /**
     * Keeps all values.
     */
    NOFILTER((ExportFilter & Serializable) List::of);

    private final ExportFilter filter;

    CommonFilters(final ExportFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the {@link ExportFilter}
     */
    public ExportFilter getFilteringPolicy() {
        return filter;
    }

    /**
     * @param input a {@link String} matching a {@link ExportFilter} name (one of the values of {@link CommonFilters})
     * @return the corresponding {@link ExportFilter}
     */
    public static ExportFilter fromString(final String input) {
        return valueOf(Objects.requireNonNull(input).toUpperCase(Locale.ENGLISH)).getFilteringPolicy();
    }

}
