package it.unibo.alchemist.loader.variables;

import java.util.stream.Collectors;

/**
 * A variable stub, with a default {@link #toString()} method.
 */
public abstract class PrintableVariable implements Variable {

    private static final long serialVersionUID = 0L;

    @Override
    public String toString() {
        return '[' + stream().mapToObj(Double::toString).collect(Collectors.joining(",")) + ']';
    }

}
