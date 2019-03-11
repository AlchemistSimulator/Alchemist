/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * An entity which is able to produce an Alchemist {@link Environment}, possibly
 * with user defined variable values.
 */
public interface Loader extends Serializable {

    /**
     * @param <T>
     *            concentration type
     * @return an {@link Environment} with all the variables set at their
     *         default values
     */
    <T, P extends Position<P>> Environment<T, P> getDefault();

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
     * @return an {@link Environment} with all the variables set at the
     *         specified values. If the value is unspecified, the default is
     *         used instead
     */
    <T, P extends Position<P>> Environment<T, P> getWith(Map<String, ?> values);

    /**
     * @return The data extractors
     */
    List<Extractor> getDataExtractors();

    /**
     * 
     * @return dependencies files
     */
    List<String> getDependencies();

}
