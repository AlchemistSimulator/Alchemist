package it.unibo.alchemist.loader.variables;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.FastMath;

/**
 * A variable ranging geometrically (exponentially) in a range. Ideal for log-scale comparisons.
 * 
 * e.g. a {@link GeometricVariable} with minimum = 1, maximum = 100 and samples = 5 will range over [1, ~3.16, 10, ~31.62 100].
 * 
 * Both min and max must be strictly bigger than 0.
 */
public class GeometricVariable extends PrintableVariable {

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
        if (min >= max) {
            throw new IllegalArgumentException("min (" + min + ") can't be bigger than max (" + max + ")");
        }
        if (min <= 0d || max <= 0) {
            throw new IllegalArgumentException("Both minimum and maximum must be bigger than 0 for a geometric variable to work.");
        }
        if (samples <= 0) {
            throw new IllegalArgumentException("At least one sample is required.");
        }
        this.def = def;
        this.min = min;
        this.max = max;
        this.maxSamples = samples;
    }

    @Override
    public double getDefault() {
        return def;
    }

    @Override
    public DoubleStream stream() {
        return IntStream.range(0, maxSamples)
                .mapToDouble(s -> min * FastMath.pow(max / min, (double) s / (maxSamples - 1)));
    }

    @Override
    public String toString() {
        return '[' + stream().mapToObj(Double::toString).collect(Collectors.joining(",")) + ']';
    }

}
