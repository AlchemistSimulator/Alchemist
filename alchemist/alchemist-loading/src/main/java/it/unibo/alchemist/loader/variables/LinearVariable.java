package it.unibo.alchemist.loader.variables;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a linear variable, namely a variable whose values span
 * linearly between minimum and maximum.
 */
public class LinearVariable extends PrintableVariable<Double> {

    private static final long serialVersionUID = 2462199794377640948L;
    private static final Logger L = LoggerFactory.getLogger(LinearVariable.class);
    private final double min, max, step, def;

    /**
     * @param def default value
     * @param min minimum (inclusive)
     * @param max maximum (inclusive)
     * @param step number of steps
     */
    public LinearVariable(final double def, final double min, final double max, final double step) {
        if (max < min) {
            throw new IllegalArgumentException("The maximum value is smaller than the minimum.");
        }
        if (def > max || def < min) {
            L.warn("The provided default value for a linear variable ({}) is out of bounds: [{}, {}]", def, min, max);
        }
        this.min = min;
        this.max = max;
        this.step = step;
        this.def = def;
    }

    @Override
    public Double getDefault() {
        return def;
    }

    @Override
    public long steps() {
        final long num = (long) Math.ceil((max - min) / step);
        if (min + step * num <= max) {
            return num + 1;
        }
        return num;
    }

    @Override
    public Stream<Double> stream() {
        return DoubleStream.iterate(min, x -> x + step).limit(steps()).boxed();
    }

}
