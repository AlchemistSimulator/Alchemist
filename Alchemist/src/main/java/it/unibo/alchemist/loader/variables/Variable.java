package it.unibo.alchemist.loader.variables;

import java.io.Serializable;
import java.util.Iterator;
import java.util.stream.DoubleStream;

/**
 * A variable simulation value, that provides a range of values for batches, and
 * a default value for single-shot runs.
 */
public interface Variable extends Serializable, Iterable<Double> {

    @Override
    default Iterator<Double> iterator() {
        return stream().iterator();
    }

    /**
     * @return the minimum value
     */
    default double min() {
        return stream().min().getAsDouble();
    }

    /**
     * @return the maximum value
     */
    default double max() {
        return stream().max().getAsDouble();
    }

    /**
     * @return the number of different values this {@link Variable} may yield
     */
    default long steps() {
        return stream().count();
    };

    /**
     * @return the default value for this {@link Variable}
     */
    double getDefault();

    /**
     * @return a view of the values of this variable as {@link DoubleStream}.
     */
    DoubleStream stream();

}
