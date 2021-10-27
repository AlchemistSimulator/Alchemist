/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader;

import it.unibo.alchemist.loader.export.GenericExporter;
import it.unibo.alchemist.loader.variables.DependentVariable;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.interfaces.Position;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An entity which is able to produce an Alchemist {@link InitializedEnvironment}, resolving user defined variable values.
 */
public interface Loader extends Serializable {

    /**
     * @param <T>
     *            concentration type
     * @param <P> position type
     * @return an {@link InitializedEnvironment} with all the variables set at their
     *         default values
     */
    default <T, P extends Position<P>> InitializedEnvironment<T, P> getDefault() {
        return getWith(Collections.emptyMap());
    }

    /**
     * Allows to access the currently defined dependent variable (those variables whose value can be determined given a
     * valid set of values for the free variables).
     *
     * @return a {@link Map} between variable names and their actual
     * representation
     */
    Map<String, DependentVariable<?>> getDependentVariables();

    /**
     * @return a {@link Map} between variable names and their actual
     *         representation
     */
    Map<String, Variable<?>> getVariables();

    /**
     * @param values
     *            a map specifying name-value bindings for the variables in this
     *            scenario
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     * @return an {@link InitializedEnvironment} with all the variables set at the
     *         specified values. If the value is unspecified, the default is
     *         used instead
     */
    <T, P extends Position<P>> InitializedEnvironment<T, P> getWith(Map<String, ?> values);

    /**
     * Allows to access the currently defined constants, namely variables defined in the simulation file whose value is
     * constant and does not depend on the value of any free variable (directly or indirectly).
     *
     * @return a {@link Map} between variable names and their computed value
     */
    Map<String, Object> getConstants();

    /**
     * 
     * @return dependencies files
     */
    List<String> getRemoteDependencies();
}
