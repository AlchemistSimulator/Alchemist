package it.unibo.alchemist.loader.export;

import java.util.stream.DoubleStream;

/**
 * Expresses a flat map operation over a double.
 */
@FunctionalInterface
public interface FilteringPolicy {

    /**
     * From a single value, builds a stream of values.
     * 
     * @param value
     *            the input value
     * @return a stream of double values. In most cases, it will be a
     *         {@link DoubleStream} of a single value, but may easily be an
     *         empty {@link DoubleStream} (in case the value must be filtered).
     *         Also, the case in which a single value gets mapped onto multiple
     *         values is supported by this interface.
     */
    DoubleStream apply(double value);

}
