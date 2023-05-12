/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.variables;

import org.apache.commons.math3.util.FastMath;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A variable ranging geometrically (exponentially) in a range. Ideal for log-scale comparisons.
 * E.g., a {@link GeometricVariable} with minimum = 1,
 * maximum = 100 and samples = 5 will range over
 * [1, ~3.16, 10, ~31.62 100].
 * Both min and max must be strictly bigger than 0.
 */
public final class GeometricVariable extends PrintableVariable<Double> {

    private static final long serialVersionUID = 1L;
    private final double def;
    private final int maxSamples;
    private final double min, max;

    /**
     * @param def
     *            default value
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param samples
     *            number of samples (must be bigger than zero)
     */
    public GeometricVariable(final double def, final double min, final double max, final int samples) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") can't be bigger than max (" + max + ")");
        }
        if (min <= 0d || max <= 0) {
            throw new IllegalArgumentException(
                "Both minimum and maximum must be bigger than 0 for a geometric variable to work."
            );
        }
        if (samples <= 0) {
            throw new IllegalArgumentException("At least one sample is required.");
        }
        if (min == max && samples != 1) {
            throw new IllegalArgumentException(
                "Only a single sample can be produced if min and max are exactly equal. (min="
                    + min + ", max=" + max + ", samples=" + samples
            );
        }
        this.def = def;
        this.min = min;
        this.max = max;
        this.maxSamples = samples;
    }

    @Override
    public Double getDefault() {
        return def;
    }

    @Override
    public Stream<Double> stream() {
        return IntStream.range(0, maxSamples)
            .mapToDouble(s -> min * FastMath.pow(max / min, (double) s / Math.max(1, maxSamples - 1)))
            .boxed();
    }

    @Override
    public String toString() {
        return '[' + stream().map(Object::toString).collect(Collectors.joining(",")) + ']';
    }

}
