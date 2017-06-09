package it.unibo.alchemist.loader.variables;

import java.util.stream.Collectors;

/**
 * A variable stub, with a default {@link #toString()} method.
 *
 * @param <V>
 */
public abstract class PrintableVariable<V> implements Variable<V> {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return '[' + stream().map(Object::toString).collect(Collectors.joining(",")) + ']';
    }

}
