package it.unibo.alchemist.loader.variables;

import java.io.Serializable;
import java.util.Map;

/**
 * A dependent variable, namely a variable whose value can be obtained given the
 * values of other variables.
 *
 * @param <V>
 */
@FunctionalInterface
public interface DependentVariable<V> extends Serializable {

    /**
     * Given the current controlled variables, computes the current values for
     * this variable.
     * 
     * @param variables
     *            a mapping between variable names and values
     * @return the value for this value
     * @throws IllegalStateException
     *             if the value can not be computed, e.g. because there are
     *             unassigned required variables
     */
    V getWith(Map<String, Object> variables);

}
