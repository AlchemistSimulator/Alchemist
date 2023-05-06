/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary;

import java.io.Serializable;
import java.util.Map;

/**
 * A dependent variable, namely a variable whose value can be obtained given the
 * values of other variables.
 *
 * @param <V> value type of the variable
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
