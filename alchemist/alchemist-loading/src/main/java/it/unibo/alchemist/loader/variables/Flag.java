package it.unibo.alchemist.loader.variables;

import java.util.stream.DoubleStream;

/**
 * This variable is a flag. Being booleans not a valid data type in charts, this
 * variable just outputs 0 and 1. This is equivalent to a {@link LinearVariable}
 * with two samples ranging from 0 to 1.
 */
public class Flag extends PrintableVariable {

    private static final long serialVersionUID = 1L;
    private final double defVal;

    /**
     * @param def
     *            the default value
     */
    public Flag(final boolean def) {
        this.defVal = def ? 1.0 : 0.0;
    }

    @Override
    public double getDefault() {
        return defVal;
    }

    @Override
    public DoubleStream stream() {
        return DoubleStream.of(0.0, 1.0);
    }

}
