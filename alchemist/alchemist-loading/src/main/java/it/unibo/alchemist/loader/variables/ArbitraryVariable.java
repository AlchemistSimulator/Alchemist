package it.unibo.alchemist.loader.variables;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * A variable spanning over an arbitrary set of values.
 */
public class ArbitraryVariable extends PrintableVariable<Serializable> {

    private static final long serialVersionUID = 1L;
    private final Serializable def;
    private final Serializable[] vals;

    private ArbitraryVariable(final Serializable def, final boolean copy, final Serializable... values) {
        this.def = def;
        vals = copy ? Arrays.copyOf(values, values.length) : values;
        Arrays.sort(vals);
    }

    /**
     * @param def
     *            the default value
     * @param values
     *            all the values this variable may yield
     */
    public ArbitraryVariable(final double def, final double... values) {
        this(def, true, values);
    }

    /**
     * @param def
     *            the default value
     * @param values
     *            all the values this variable may yield
     */
    public ArbitraryVariable(final double def, final List<? extends Number> values) {
        this(def, false, values.stream().mapToDouble(Number::doubleValue).distinct().toArray());
    }

    @Override
    public Serializable getDefault() {
        return def;
    }

    @Override
    public Stream<Serializable> stream() {
        return Arrays.stream(vals).distinct().sorted();
    }

}
