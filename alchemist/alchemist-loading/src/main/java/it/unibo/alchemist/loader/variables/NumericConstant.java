package it.unibo.alchemist.loader.variables;

import java.util.Map;

/**
 * A numeric constant.
 */
public class NumericConstant implements DependentVariable<Number> {

    private static final long serialVersionUID = 1L;
    private final Number internal;

    /**
     * @param n the number
     */
    public NumericConstant(final Number n) {
        internal = n;
    }

    @Override
    public Number getWith(final Map<String, Object> variables) {
        return internal;
    }

}
